package grails.plugin.gson

import com.google.gson.Gson
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.google.gson.GsonBuilder

class GsonFactory implements GrailsApplicationAware {

	GrailsApplication grailsApplication

	GsonFactory() {}

	GsonFactory(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
	}

	Gson createGson() {
		def builder = new GsonBuilder()
		for (domainClass in grailsApplication.getDomainClasses()) {
			println "registering adapter for $domainClass"
			builder.registerTypeAdapter domainClass.clazz, new GrailsDomainDeserializer(grailsApplication: grailsApplication)
		}
		builder.create()
	}

}
