package grails.plugin.gson

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import com.google.gson.*

import java.lang.reflect.*
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * A _JsonDeserializer_ implementation that works on Grails domain objects.
 *
 * If the JSON element contains an _id_ property then the domain instance is
 * retrieved from the database, otherwise a new instance is constructed. This
 * means you can deserialize a JSON HTTP request into a new domain instance or
 * an update to an existing one.
 */
@Slf4j
class GrailsDomainDeserializer implements JsonDeserializer {

	GrailsDomainClass domainClass

	GrailsDomainDeserializer(GrailsDomainClass domainClass) {
		this.domainClass = domainClass
	}

	Object deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
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
		def id = context.deserialize(jsonObject.get('id'), getPropertyType('id'))
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


	private static class DomainClassPropertyParameterizedType implements ParameterizedType {

		private final GrailsDomainClassProperty property

		static ParameterizedType forProperty(GrailsDomainClassProperty property) {
			new DomainClassPropertyParameterizedType(property)
		}

		private DomainClassPropertyParameterizedType(GrailsDomainClassProperty property) {
			this.property = property
		}

		Type[] getActualTypeArguments() {
			def referencedType = property.referencedPropertyType
			if (Map.isAssignableFrom(property.type)) {
				[String, referencedType] as Type[]
			} else {
				[referencedType] as Type[]
			}
		}

		Type getRawType() {
			property.type
		}

		Type getOwnerType() {
			null
		}

		@Override
		String toString() {
			"$rawType.name<${actualTypeArguments.name.join(', ')}>"
		}
	}

}
