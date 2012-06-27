package grails.plugin.gson

import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import com.google.gson.*
import org.codehaus.groovy.grails.commons.*

import java.lang.reflect.*

/**
 * A deserializer that works on Grails domain objects. If the JSON element contains an _id_ property then the domain
 * instance is retrieved from the database, otherwise a new instance is constructed. This means you can deserialize a
 * JSON HTTP request into a new domain instance or an update to an existing one.
 */
class GrailsDomainDeserializer implements JsonDeserializer, GrailsApplicationAware {

	GrailsApplication grailsApplication

    Object deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
		def domainClass = getDomainClass(type)
        def jsonObject = element.asJsonObject
        def id = context.deserialize(jsonObject.get('id'), getPropertyType(domainClass, 'id'))
        def instance = id ? type.get(id) : type.newInstance()
        for (prop in jsonObject.entrySet()) {
			Type propertyType = getPropertyType(domainClass, prop.key)
			println "deserializing $prop.key $prop.value ($propertyType)"
			instance.properties[prop.key] = context.deserialize(prop.value, propertyType)
        }
        instance
    }

	private Type getPropertyType(GrailsDomainClass domainClass, String name) {
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

	private GrailsDomainClass getDomainClass(Type type) {
		grailsApplication.getDomainClass(type.name)
	}

}
