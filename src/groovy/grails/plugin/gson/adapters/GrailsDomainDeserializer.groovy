package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*
import static org.codehaus.groovy.grails.web.binding.DataBindingUtils.bindObjectToDomainInstance

/**
 * A _JsonDeserializer_ implementation that works on Grails domain objects.
 *
 * If the JSON element contains an _id_ property then the domain instance is
 * retrieved from the database, otherwise a new instance is constructed. This
 * means you can deserialize a JSON HTTP request into a new domain instance or
 * an update to an existing one.
 */
@TupleConstructor
@Slf4j
class GrailsDomainDeserializer<T> implements JsonDeserializer<T> {

	final GrailsApplication grailsApplication

	T deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
		def domainClass = getDomainClassFor(type)
		def jsonObject = element.asJsonObject
		T instance = getOrCreateInstance(jsonObject, domainClass, context)

		def properties = jsonObject.entrySet().collectEntries { property ->
			def propertyType = getPropertyType(domainClass, property.key)
			[(property.key): context.deserialize(property.value, propertyType)]
		}
		bindObjectToDomainInstance domainClass, instance, properties
		instance
	}

	private T getOrCreateInstance(JsonObject jsonObject, GrailsDomainClass domainClass, JsonDeserializationContext context) {
		def identityProp = domainClass.identifier
		def id = context.deserialize(jsonObject.get(identityProp.name), identityProp.type)
		id ? domainClass.clazz.get(id) : domainClass.clazz.newInstance()
	}

	private Type getPropertyType(GrailsDomainClass domainClass, String name) {
		def domainClassProperty = domainClass.getPropertyByName(name)

		if (domainClassProperty.manyToMany || domainClassProperty.oneToMany) {
			DomainClassPropertyParameterizedType.forProperty(domainClassProperty)
		} else {
			domainClassProperty.type
		}
	}

	private GrailsDomainClass getDomainClassFor(Type type) {
		// TODO: may need to cache this
		grailsApplication.getDomainClass(type.name)
	}

}
