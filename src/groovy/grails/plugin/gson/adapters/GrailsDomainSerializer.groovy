package grails.plugin.gson.adapters

import java.lang.reflect.Type
import com.google.gson.*
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.apache.commons.beanutils.PropertyUtils
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.GrailsConfig
import org.codehaus.groovy.grails.support.proxy.EntityProxyHandler

@TupleConstructor
@Slf4j
class GrailsDomainSerializer<T> implements JsonSerializer<T> {

	final GrailsApplication grailsApplication
	final EntityProxyHandler proxyHandler

	private final Stack<GrailsDomainClassProperty> circularityStack = new Stack<GrailsDomainClassProperty>()

	@Override
	JsonElement serialize(T instance, Type type, JsonSerializationContext context) {
		def element = new JsonObject()
		if (shouldOutputClass()) {
			element.add 'class', context.serialize(instance.getClass().name)
		}
		eachUnvisitedProperty(instance) { GrailsDomainClassProperty property ->
			def field = instance.getClass().getDeclaredField(property.name)
			def value = PropertyUtils.getProperty(instance, property.name)
			def elementName = fieldNamingStrategy.translateName(field)
			if (!proxyHandler.isInitialized(instance, property.name)) {
				if (shouldResolveProxy()) {
					log.debug "unwrapping proxy for $property.domainClass.shortName.$property.name"
					value = proxyHandler.unwrapIfProxy(value)
					element.add elementName, context.serialize(value, property.type)
				} else if (property.oneToMany || property.manyToMany) {
					log.debug "skipping proxied collection $property.domainClass.shortName.$property.name"
				} else {
					log.debug "not unwrapping proxy for $property.domainClass.shortName.$property.name"
					value = [id: proxyHandler.getProxyIdentifier(value)]
					element.add elementName, context.serialize(value, Map)
				}
			} else {
				element.add elementName, context.serialize(value, property.type)
			}
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
		if (shouldOutputVersion()) {
			iterator(domainClass.version)
		}
		for (property in domainClass.persistentProperties) iterator(property)
	}

	private void handleCircularReference(GrailsDomainClassProperty property) {
		log.debug "already dealt with $property.domainClass.shortName.$property.name"
	}

	private GrailsDomainClass getDomainClassFor(T instance) {
		// TODO: may need to cache this
		grailsApplication.getDomainClass(instance.getClass().name)
	}

	@Lazy
	private FieldNamingStrategy fieldNamingStrategy = {
		def grailsConfig = new GrailsConfig(grailsApplication)
		grailsConfig.get('grails.converters.gson.fieldNamingPolicy', FieldNamingStrategy) ?: FieldNamingPolicy.IDENTITY
	}()

	private boolean shouldResolveProxy() {
		config.get('grails.converters.gson.resolveProxies', true)
	}

	private boolean shouldOutputClass() {
		config.get('grails.converters.gson.domain.include.class', config.get('grails.converters.domain.include.class', false))
	}

	private boolean shouldOutputVersion() {
		config.get('grails.converters.gson.domain.include.version', config.get('grails.converters.domain.include.version', false))
	}

	private GrailsConfig getConfig() {
		new GrailsConfig(grailsApplication)
	}

}
