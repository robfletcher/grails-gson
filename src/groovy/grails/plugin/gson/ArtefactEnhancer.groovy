package grails.plugin.gson

import javax.servlet.http.HttpServletRequest
import com.google.gson.*
import org.codehaus.groovy.grails.commons.GrailsApplication
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
			controller.metaClass.render = { GSON gson ->
				gson.render delegate.response
			}
		}
	}

	void enhanceDomains() {
		for (domainClass in grailsApplication.domainClasses) {
			domainClass.metaClass.constructor = { JsonElement json ->
				gson.fromJson(json, delegate)
			}
			domainClass.metaClass.setProperties = { JsonElement json ->
				bindObjectToDomainInstance domainClass, delegate, gson.fromJson(json, Map)
			}
		}
	}

	void enhanceRequest() {
		HttpServletRequest.metaClass.getGSON = {->
			def requestBody = new BufferedReader(delegate.reader)
			new JsonParser().parse(requestBody)
		}
	}
}
