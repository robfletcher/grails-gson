package grails.plugin.gson.converters

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.converters.AbstractParsingParameterCreationListener
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

@Slf4j
class GsonParsingParameterCreationListener extends AbstractParsingParameterCreationListener {

	@Override
	void paramsCreated(GrailsParameterMap params) {
		log.error 'lol whatever'
	}

}
