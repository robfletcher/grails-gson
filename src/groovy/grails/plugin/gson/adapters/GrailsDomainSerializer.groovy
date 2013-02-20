package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*

@TupleConstructor(includeFields = true)
@Slf4j
class GrailsDomainSerializer<T> implements JsonSerializer<T> {

	private final GrailsDomainClass domainClass
	private final Collection referenceCache = new HashSet()

	@Override
	JsonElement serialize(T instance, Type type, JsonSerializationContext context) {
		if (instance in referenceCache) {
			log.debug 'circular reference detected...'
			null
		} else {
			referenceCache << instance
			serializeEntity instance, context
		}
	}

	private JsonElement serializeEntity(T instance, context) {
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
