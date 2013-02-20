package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*

@TupleConstructor
@Slf4j
class GrailsDomainSerializer<T> implements JsonSerializer<T> {

	final GrailsApplication grailsApplication

	private final Stack<GrailsDomainClassProperty> circularityStack = new Stack<GrailsDomainClassProperty>()

	@Override
	JsonElement serialize(T instance, Type type, JsonSerializationContext context) {
		def element = new JsonObject()
		eachUnvisitedProperty(instance) { GrailsDomainClassProperty property ->
			element.add property.name, context.serialize(instance[property.name], property.type)
		}
		element
	}

	private void eachUnvisitedProperty(T instance, Closure iterator) {
		eachProperty(instance) { GrailsDomainClassProperty property ->
			if (property in circularityStack) {
				handleCircularReference property
			} else if (property.bidirectional) {
				circularityStack.push property.otherSide
				iterator property
				circularityStack.pop()
			} else {
				iterator property
			}
		}
	}

	private void eachProperty(T instance, Closure iterator) {
		def domainClass = getDomainClassFor(instance)
		iterator(domainClass.identifier)
		for (property in domainClass.persistentProperties) iterator(property)
	}

	private void handleCircularReference(GrailsDomainClassProperty property) {
		log.debug "already dealt with ${property.domainClass.shortName}.${property.name}"
	}

	private GrailsDomainClass getDomainClassFor(T instance) {
		// TODO: may need to cache this
		grailsApplication.getDomainClass(instance.getClass().name)
	}

}
