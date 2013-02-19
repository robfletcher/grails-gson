package grails.plugin.gson

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.*

/**
 * Ensures that Hibernate proxies are initialized when serializing domain instances.
 *
 * Based on code from http://stackoverflow.com/questions/13459718/could-not-serialize-object-cause-of-hibernateproxy#answer-13525550
 */
class HibernateProxyAdapter extends TypeAdapter {

	static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		<T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			Class.forName('org.hibernate.proxy.HibernateProxy').isAssignableFrom(type.rawType) ? new HibernateProxyAdapter(gson) : null
		}
	};

	private final Gson context

	HibernateProxyAdapter(Gson context) {
		this.context = context
	}

	@Override
	void write(JsonWriter out, value) throws IOException {
		if (value == null) {
			out.nullValue()
		} else {
			def baseType = org.hibernate.Hibernate.getClass(value)
			def delegate = context.getAdapter(TypeToken.get(baseType))
			def unproxiedValue = value.hibernateLazyInitializer.implementation
			delegate.write out, unproxiedValue
		}
	}

	@Override
	Object read(JsonReader jsonReader) throws IOException {
		throw new UnsupportedOperationException()
	}
}
