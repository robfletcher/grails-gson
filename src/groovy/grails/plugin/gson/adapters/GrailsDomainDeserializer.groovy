package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import grails.util.GrailsNameUtils
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
		if (instance) {
			bindJsonToInstance jsonObject, domainClass, instance, context
			instance
		} else {
			null
		}
	}

	void bindJsonToInstance(JsonObject jsonObject, GrailsDomainClass domainClass, T instance, context) {
		def jsonEntries = jsonObject.entrySet().findAll { property ->
			if (property.key == domainClass.identifier.name || domainClass.hasPersistentProperty(property.key)) {
				true
			} else {
				log.debug "Unknown property ${domainClass.shortName}.${property.key} found in json"
				false
			}
		}
		def properties = jsonEntries.collectEntries { property ->
			def propertyType = getPropertyType(domainClass, property.key)
			[(property.key): context.deserialize(property.value, propertyType)]
		}
		bindObjectToDomainInstance domainClass, instance, properties

		processBidirectionalAssociations domainClass, instance
	}

	private void processBidirectionalAssociations(GrailsDomainClass domainClass, T instance) {
		domainClass.persistentProperties.each { property ->
			if (property.bidirectional) {
				bindOwner instance."$property.name", property.otherSide, instance
			}
		}
	}

	private void bindOwner(value, GrailsDomainClassProperty property, T owner) {
		if (value == null) return

		if (value instanceof Map) {
			value = value.values()
		}

		if (property.manyToOne) {
			value.each {
				it[property.name] = owner
			}
		} else if (property.oneToMany) {
			def addToMethodName = getAddToMethodName(property)
			value."$addToMethodName" owner
		} else if (property.manyToMany) {
			def addToMethodName = getAddToMethodName(property)
			value.each {
				it."$addToMethodName" owner
			}
		} else {
			value."$property.name" = owner
		}
	}

	private String getAddToMethodName(GrailsDomainClassProperty property) {
		"addTo${GrailsNameUtils.getClassName(property.name)}"
	}


	private T getOrCreateInstance(JsonObject jsonObject, GrailsDomainClass domainClass, JsonDeserializationContext context) {
		def identityProp = domainClass.identifier
		def id = context.deserialize(jsonObject.get(identityProp.name), identityProp.type)
		id ? domainClass.clazz.get(id) : domainClass.clazz.newInstance()
	}

	private Type getPropertyType(GrailsDomainClass domainClass, String name) {
		def domainClassProperty = domainClass.hasProperty(name) ? domainClass.getPropertyByName(name) : null
		if (domainClassProperty) {
			DomainClassPropertyParameterizedType.forProperty(domainClassProperty)
		}
	}

	private GrailsDomainClass getDomainClassFor(Type type) {
		// TODO: may need to cache this
		grailsApplication.getDomainClass(type.name)
	}

}
