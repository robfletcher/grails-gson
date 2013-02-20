package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * A _JsonDeserializer_ implementation that works on Grails domain objects.
 *
 * If the JSON element contains an _id_ property then the domain instance is
 * retrieved from the database, otherwise a new instance is constructed. This
 * means you can deserialize a JSON HTTP request into a new domain instance or
 * an update to an existing one.
 */
@Slf4j
class GrailsDomainDeserializer<T> implements JsonDeserializer<T> {

	private final GrailsApplication grailsApplication

	GrailsDomainDeserializer(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
	}

	T deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
		def domainClass = getDomainClassFor(type)
		def jsonObject = element.asJsonObject
		def instance = getOrCreateInstance(jsonObject, domainClass, context)
		for (prop in jsonObject.entrySet()) {
			def propertyType = getPropertyType(domainClass, prop.key)
			log.debug "deserializing $prop.key to $propertyType"
			instance[prop.key] = context.deserialize(prop.value, propertyType)
		}
		instance
	}

	private getOrCreateInstance(JsonObject jsonObject, GrailsDomainClass domainClass, JsonDeserializationContext context) {
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
