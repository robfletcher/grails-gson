package grails.plugin.gson.converters

import com.google.gson.GsonBuilder
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.converters.AbstractParsingParameterCreationListener
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

@TupleConstructor(includeFields = true)
@Slf4j
class GsonParsingParameterCreationListener extends AbstractParsingParameterCreationListener {

	private final GsonBuilder gsonBuilder

	@Override
	void paramsCreated(GrailsParameterMap params) {
		def request = params.getRequest()
		if (GSON.isJson(request)) {
			try {
				def json = GSON.parse(request)

				for (entry in gsonBuilder.create().fromJson(json, Map)) {
					params[entry.key] = entry.value
				}
			} catch (Exception e) {
				log.error 'exception parsing request as JSON', e
			}
		}
	}

}
