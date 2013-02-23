package grails.plugin.gson.converters

import com.google.gson.JsonObject
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification
import static grails.plugin.gson.converters.GSON.CACHED_GSON

@TestMixin(ControllerUnitTestMixin)
class GsonParsingParameterCreationListenerSpec extends Specification {

	def listener = new GsonParsingParameterCreationListener()

	void 'does nothing if the request content is not JSON'() {
		when:
		listener.paramsCreated(params)

		then:
		request.getAttribute(CACHED_GSON) == null
	}

	void 'parses and caches a JsonElement if request content is JSON'() {
		given:
		request.contentType = 'application/json'
		request.content = '{"message":"Namaste"}'.bytes

		when:
		listener.paramsCreated(params)

		then:
		with(request.getAttribute(CACHED_GSON)) { json ->
			json instanceof JsonObject
			json.message.asString == 'Namaste'
		}
	}

}
