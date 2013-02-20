package grails.plugin.gson

import java.lang.reflect.Type
import com.google.gson.*
import grails.plugin.gson.adapters.*
import grails.plugin.gson.support.hibernate.HibernateProxyAdapter
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.*
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

/**
 * A factory for _Gson_ instances that automatically registers
 * _JsonDeserializer_ instances for all domain classes in the application.
 */
@Slf4j
@TupleConstructor
class GsonFactory implements GrailsApplicationAware, PluginManagerAware {

	GrailsApplication grailsApplication
	GrailsPluginManager pluginManager
	private final Map<Type, ?> typeAdapters = [:]

	GsonFactory() {}

	Gson createGson() {
		def builder = new GsonBuilder()

		for (domainClass in grailsApplication.getDomainClasses()) {
			log.debug "registering adapter for $domainClass.name"
			builder.registerTypeAdapter domainClass.clazz, new GrailsDomainDeserializer(domainClass)
		}

		if (pluginManager.hasGrailsPlugin('hibernate')) {
			builder.registerTypeAdapterFactory(HibernateProxyAdapter.FACTORY)
		}

		builder.addSerializationExclusionStrategy new GrailsDomainExclusionStrategy(grailsApplication)

		for (entry in typeAdapters) {
			builder.registerTypeAdapter entry.key, entry.value
		}

		builder.create()
	}

	void registerTypeAdapter(Type type, adapter) {
		typeAdapters[type] = adapter
	}
}
