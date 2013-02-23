package grails.plugin.gson.converters

import com.google.gson.GsonBuilder
import grails.util.GrailsNameUtils
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

				def map = gsonBuilder.create().fromJson(json, Map)

				if (map.containsKey('class')) {
					params[GrailsNameUtils.getPropertyName(map['class'])] = map
				} else {
					for (entry in map) {
						params[entry.key] = entry.value
					}
				}

				processNestedKeys map, params
			} catch (Exception e) {
				log.error 'exception parsing request as JSON', e
			}
		}
	}

	private void processNestedKeys(Map map, GrailsParameterMap params) {
		def target = [:]
		createFlattenedKeys(map, map, target)
		for (entry in target) {
			if (!map[entry.key]) {
				params[entry.key] = entry.value
			}
		}
	}
}
