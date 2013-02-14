package grails.plugin.gson

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import com.google.gson.*

import static org.codehaus.groovy.grails.web.binding.DataBindingUtils.bindObjectToDomainInstance

/**
 * Adds GSON meta methods and properties to Grails artifacts.
 */
class ArtefactEnhancer {

	private final GrailsApplication grailsApplication
	private final Gson gson

	ArtefactEnhancer(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
		gson = new GsonFactory(grailsApplication).createGson()
	}

	void enhanceControllers() {
		for (controller in grailsApplication.controllerClasses) {
			controller.clazz.metaClass.render = { GSON gson ->
				gson.render delegate.response
			}
		}
	}

	void enhanceDomains() {
		for (domainClass in grailsApplication.domainClasses) {
			domainClass.clazz.metaClass.constructor = { JsonElement json ->
				gson.fromJson(json, delegate)
			}
			domainClass.clazz.metaClass.setProperties = { JsonElement json ->
				bindObjectToDomainInstance domainClass, delegate, gson.fromJson(json, Map)
			}
		}
	}

	void enhanceRequest() {
		GrailsMockHttpServletRequest.metaClass.getGSON = { ->
			new JsonParser().parse(delegate.reader)
		}
	}
}
