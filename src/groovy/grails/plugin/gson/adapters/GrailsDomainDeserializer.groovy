package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * A _JsonDeserializer_ implementation that works on Grails domain objects.
 *
 * If the JSON element contains an _id_ property then the domain instance is
 * retrieved from the database, otherwise a new instance is constructed. This
 * means you can deserialize a JSON HTTP request into a new domain instance or
 * an update to an existing one.
 */
@TupleConstructor(includeFields = true)
@Slf4j
class GrailsDomainDeserializer<T> implements JsonDeserializer<T> {

	GrailsDomainClass domainClass

	T deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
		def jsonObject = element.asJsonObject
		def instance = getOrCreateInstance(jsonObject, type, context)
		for (prop in jsonObject.entrySet()) {
			def propertyType = getPropertyType(prop.key)
			log.debug "deserializing $prop.key to $propertyType"
			instance[prop.key] = context.deserialize(prop.value, propertyType)
		}
		instance
	}

	private getOrCreateInstance(JsonObject jsonObject, Type type, JsonDeserializationContext context) {
        def identityProp = domainClass.identifier
		def id = context.deserialize(jsonObject.get(identityProp.name), identityProp.type)
		id ? type.get(id) : type.newInstance()
	}

	private Type getPropertyType(String name) {
		def domainClassProperty = domainClass.getPropertyByName(name)

		if (domainClassProperty.manyToMany || domainClassProperty.oneToMany) {
			DomainClassPropertyParameterizedType.forProperty(domainClassProperty)
		} else {
			domainClassProperty.type
		}
	}

}
