package grails.plugin.gson.api

import javax.servlet.http.HttpServletRequest
import com.google.gson.*
import grails.plugin.gson.adapters.GrailsDomainDeserializer
import grails.plugin.gson.converters.GSON
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.*

/**
 * Adds GSON meta methods and properties to Grails artifacts.
 */
@Slf4j
class ArtefactEnhancer {

	private final GrailsApplication grailsApplication
	private final GrailsDomainDeserializer domainDeserializer
	private final Gson gson

	ArtefactEnhancer(GrailsApplication grailsApplication, GsonBuilder gsonBuilder, GrailsDomainDeserializer domainDeserializer) {
		this.grailsApplication = grailsApplication
		this.domainDeserializer = domainDeserializer
		gson = gsonBuilder.create()
	}

	void enhanceControllers() {
		for (controller in grailsApplication.controllerClasses) {
			controller.metaClass.render = { GSON gson ->
				gson.render delegate.response
			}
		}
	}

	void enhanceDomains() {
		grailsApplication.domainClasses.each { GrailsDomainClass domainClass ->
			domainClass.metaClass.constructor = { JsonObject json ->
				gson.fromJson json, delegate
			}
			domainClass.metaClass.setProperties = { JsonObject json ->
				domainDeserializer.bindJsonToInstance json, domainClass, delegate, gson.deserializationContext
			}
		}
	}

	void enhanceRequest() {
		def requestMetaClass = GroovySystem.metaClassRegistry.getMetaClass(HttpServletRequest)
		requestMetaClass.getGSON = {->
			GSON.parse delegate
		}
	}
}
