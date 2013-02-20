package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*

@Slf4j
class GrailsDomainSerializer<T> implements JsonSerializer<T> {

	private final GrailsDomainClass domainClass

	GrailsDomainSerializer(GrailsDomainClass domainClass) {
		this.domainClass = domainClass
	}

	@Override
	JsonElement serialize(T instance, Type type, JsonSerializationContext context) {
		serializeEntity instance, context
	}

	private JsonElement serializeEntity(T instance, JsonSerializationContext context) {
		def element = new JsonObject()
		eachProperty { GrailsDomainClassProperty property ->
			if (property.isAssociation() && property.isBidirectional() && !property.isOwningSide()) {
				def referencedIdProperty = property.otherSide.domainClass.identifier
				def referencedId = instance[property.name][referencedIdProperty.name]
				def value = [(referencedIdProperty.name): referencedId]
				element.add property.name, context.serialize(value, Map)
			} else {
				def value = instance[property.name]
				element.add property.name, context.serialize(value, property.type)
			}
		}
		element
	}

	private void eachProperty(Closure iterator) {
		iterator(domainClass.identifier)
		for (property in domainClass.persistentProperties) iterator(property)
	}

}
