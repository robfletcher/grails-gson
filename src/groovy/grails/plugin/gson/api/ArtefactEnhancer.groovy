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
		for (domainClass in grailsApplication.domainClasses) {
			enhanceDomain domainClass
		}
	}

	void enhanceDomain(GrailsClass domainClass) {
		final mc = domainClass.metaClass

		mc.constructor = { JsonObject json ->
			gson.fromJson json, delegate
		}

		// I hate doing this but Groovy does not allow overloading of the method dispatched to by the assignment operator.
		// Although you *can* overload `setProperties` the assignment operator will just dispatch to the last `setProperties`
		// implementation attached to the metaClass regardless of the parameter types.
		final setPropertiesMethod = mc.pickMethod('setProperties', [Object] as Class[])
		mc.setProperties = {
			if (it instanceof JsonObject) {
				domainDeserializer.bindJsonToInstance it, domainClass, delegate, gson.deserializationContext
			} else {
				setPropertiesMethod.invoke delegate, it
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
