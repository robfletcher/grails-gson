package grails.plugin.gson.spring

import java.text.DateFormat
import com.google.gson.*
import grails.plugin.gson.adapters.*
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.GrailsConfig
import org.codehaus.groovy.grails.plugins.*
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.beans.factory.annotation.Autowired
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

	@Autowired
	final GrailsDomainSerializer domainSerializer
	@Autowired
	final GrailsDomainDeserializer domainDeserializer

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

		for (typeAdapterFactory in applicationContext.getBeansOfType(TypeAdapterFactory).values()) {
			builder.registerTypeAdapterFactory(typeAdapterFactory)
		}

		applyConfiguration builder

		builder
	}

	private void applyConfiguration(GsonBuilder builder) {
		def grailsConfig = new GrailsConfig(grailsApplication)

		def defaultPrettyPrint = grailsConfig.get("grails.converters.default.pretty.print", false)
		def prettyPrint = grailsConfig.get("grails.converters.gson.pretty.print", defaultPrettyPrint)
		if (prettyPrint) {
			builder.setPrettyPrinting()
		}

		if (grailsConfig.get('grails.converters.gson.serializeNulls', false)) {
			builder.serializeNulls()
		}

		if (grailsConfig.get('grails.converters.gson.complexMapKeySerialization', false)) {
			builder.enableComplexMapKeySerialization()
		}

		if (!grailsConfig.get('grails.converters.gson.escapeHtmlChars', true)) {
			builder.disableHtmlEscaping()
		}

		if (grailsConfig.get('grails.converters.gson.generateNonExecutableJson', false)) {
			builder.generateNonExecutableJson()
		}

		if (grailsConfig.get('grails.converters.gson.serializeSpecialFloatingPointValues', false)) {
			builder.serializeSpecialFloatingPointValues()
		}

		builder.longSerializationPolicy = grailsConfig.get('grails.converters.gson.longSerializationPolicy', LongSerializationPolicy) ?: LongSerializationPolicy.DEFAULT

		builder.fieldNamingPolicy = grailsConfig.get('grails.converters.gson.fieldNamingPolicy', FieldNamingStrategy) ?: FieldNamingPolicy.IDENTITY

		def datePattern = grailsConfig.get('grails.converters.gson.datePattern', String)
		int dateStyle = grailsConfig.get('grails.converters.gson.dateStyle', DateFormat.DEFAULT)
		int timeStyle = grailsConfig.get('grails.converters.gson.timeStyle', DateFormat.DEFAULT)
		if (datePattern) {
			builder.setDateFormat(datePattern)
		} else {
			builder.setDateFormat(dateStyle, timeStyle)
		}
	}
}
