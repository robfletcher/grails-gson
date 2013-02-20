package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import org.codehaus.groovy.grails.commons.*

@TupleConstructor(includeFields = true)
class GrailsDomainSerializer<T> implements JsonSerializer<T> {

	private final GrailsDomainClass domainClass

	@Override
	JsonElement serialize(T instance, Type type, JsonSerializationContext context) {
		def element = new JsonObject()
		eachProperty { GrailsDomainClassProperty property ->
			element.add property.name, context.serialize(instance[property.name], property.type)
		}
		element
	}

	private void eachProperty(Closure iterator) {
		iterator(domainClass.identifier)
		for (property in domainClass.persistentProperties) iterator(property)
	}

}
