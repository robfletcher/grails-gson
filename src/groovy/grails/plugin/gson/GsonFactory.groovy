package grails.plugin.gson

import java.lang.reflect.Type
import com.google.gson.*
import grails.plugin.gson.adapters.*
import grails.plugin.gson.support.hibernate.HibernateProxyAdapter
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.*
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * A factory for _Gson_ instances that automatically registers
 * _JsonDeserializer_ instances for all domain classes in the application.
 */
@Slf4j
class GsonFactory implements ApplicationContextAware, GrailsApplicationAware, PluginManagerAware {

	ApplicationContext applicationContext
	GrailsApplication grailsApplication
	GrailsPluginManager pluginManager
	private final Map<Type, ?> typeAdapters = [:]

	GsonFactory() {}

	GsonFactory(ApplicationContext applicationContext, GrailsApplication grailsApplication, GrailsPluginManager pluginManager) {
		this.applicationContext = applicationContext
		this.grailsApplication = grailsApplication
		this.pluginManager = pluginManager
	}

	Gson createGson() {
		def builder = new GsonBuilder()

		def domainSerializer = new GrailsDomainSerializer(grailsApplication)
		def domainDeserializer = new GrailsDomainDeserializer(grailsApplication)

		for (GrailsDomainClass domainClass in grailsApplication.domainClasses) {
			log.debug "registering adapters for $domainClass.name"
			builder.registerTypeAdapter domainClass.clazz, domainSerializer
			builder.registerTypeAdapter domainClass.clazz, domainDeserializer
		}

		if (pluginManager.hasGrailsPlugin('hibernate')) {
			builder.registerTypeAdapterFactory(HibernateProxyAdapter.FACTORY)
		}

		for (entry in typeAdapters) {
			builder.registerTypeAdapter entry.key, entry.value
		}

		builder.create()
	}

	void registerTypeAdapter(Type type, adapter) {
		typeAdapters[type] = adapter
	}
}
