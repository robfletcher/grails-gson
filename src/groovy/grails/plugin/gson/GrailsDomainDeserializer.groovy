package grails.plugin.gson

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import com.google.gson.*

import java.lang.reflect.*

/**
 * A deserializer that works on Grails domain objects. If the JSON element contains an _id_ property then the domain
 * instance is retrieved from the database, otherwise a new instance is constructed. This means you can deserialize a
 * JSON HTTP request into a new domain instance or an update to an existing one.
 */
@Slf4j
class GrailsDomainDeserializer implements JsonDeserializer {

	GrailsDomainClass domainClass

	GrailsDomainDeserializer(GrailsDomainClass domainClass) {
		this.domainClass = domainClass
	}

	Object deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
		def jsonObject = element.asJsonObject
		def id = context.deserialize(jsonObject.get('id'), getPropertyType('id'))
		def instance = id ? type.get(id) : type.newInstance()
		for (prop in jsonObject.entrySet()) {
			Type propertyType = getPropertyType(prop.key)
			log.debug "deserializing $prop.key $prop.value ($propertyType)"
			instance.properties[prop.key] = context.deserialize(prop.value, propertyType)
		}
		instance
	}

	private Type getPropertyType(String name) {
		def domainClassProperty = domainClass.getPropertyByName(name)
		def propertyType = domainClassProperty.type

		if (domainClassProperty.manyToMany || domainClassProperty.oneToMany) {
			def componentType = domainClassProperty.referencedPropertyType
			new ParameterizedType() {
				@Override
				Type[] getActualTypeArguments() {
					[componentType] as Type[]
				}

				@Override
				Type getRawType() {
					propertyType
				}

				@Override
				Type getOwnerType() {
					null
				}
			}
		} else {
			propertyType
		}
	}

}
