package grails.plugin.gson

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import java.lang.reflect.Type
import com.google.gson.*

/**
 * A factory for _Gson_ instances that automatically registers
 * _JsonDeserializer_ instances for all domain classes in the application.
 */
@Slf4j
class GsonFactory implements GrailsApplicationAware {

	GrailsApplication grailsApplication
    private final Map<Type, ?> typeAdapters = [:]

	GsonFactory() {}

	GsonFactory(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
	}

	Gson createGson() {
		def builder = new GsonBuilder()
		for (domainClass in grailsApplication.getDomainClasses()) {
			log.debug "registering adapter for $domainClass.name"
			builder.registerTypeAdapter domainClass.clazz, new GrailsDomainDeserializer(domainClass)
            builder.addSerializationExclusionStrategy(new GrailsDomainExclusionStrategy(domainClass))
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
