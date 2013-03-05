package grails.plugin.gson.spring

import com.google.gson.*
import grails.plugin.gson.support.hibernate.HibernateProxyAdapter
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.*
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.context.*

/**
 * A factory for _Gson_ instances that automatically registers
 * _JsonDeserializer_ instances for all domain classes in the application.
 */
@Slf4j
class GsonBuilderFactory extends AbstractFactoryBean<GsonBuilder> implements ApplicationContextAware, GrailsApplicationAware, PluginManagerAware {

	GrailsApplication grailsApplication
	ApplicationContext applicationContext
	GrailsPluginManager pluginManager

	private final JsonSerializer domainSerializer
	private final JsonDeserializer domainDeserializer

	GsonBuilderFactory(JsonSerializer domainSerializer, JsonDeserializer domainDeserializer) {
		this.domainSerializer = domainSerializer
		this.domainDeserializer = domainDeserializer
	}

	@Override
	Class<GsonBuilder> getObjectType() {
		GsonBuilder
	}

	@Override
	protected GsonBuilder createInstance() {
		def builder = new GsonBuilder()

		for (GrailsDomainClass domainClass in grailsApplication.domainClasses) {
			log.debug "registering adapters for $domainClass.name"
			builder.registerTypeAdapter domainClass.clazz, domainSerializer
			builder.registerTypeAdapter domainClass.clazz, domainDeserializer
		}

		if (pluginManager.hasGrailsPlugin('hibernate')) {
			builder.registerTypeAdapterFactory(HibernateProxyAdapter.FACTORY)
		}

		for (typeAdapterFactory in applicationContext.getBeansOfType(TypeAdapterFactory).values()) {
			builder.registerTypeAdapterFactory(typeAdapterFactory)
		}

		builder
	}
}
