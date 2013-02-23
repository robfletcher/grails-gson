package grails.plugin.gson.converters

import com.google.gson.*
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.util.GrailsWebUtil
import spock.lang.Specification
import static grails.plugin.gson.converters.GSON.CACHED_GSON

@TestMixin(ControllerUnitTestMixin)
class GsonParsingParameterCreationListenerSpec extends Specification {

	private static final String APPLICATION_JSON = GrailsWebUtil.getContentType('application/json', 'UTF-8')

	def gsonBuilder = new GsonBuilder()
	def listener = new GsonParsingParameterCreationListener(gsonBuilder)

	void 'does nothing if the request content is not JSON'() {
		when:
		listener.paramsCreated(params)

		then:
		request.getAttribute(CACHED_GSON) == null
	}

	void 'parses and caches a JsonElement if request content is JSON'() {
		given:
		request.contentType = APPLICATION_JSON
		request.content = '{"message":"Namaste"}'.getBytes('UTF-8')

		when:
		listener.paramsCreated(params)

		then:
		with(request.getAttribute(CACHED_GSON)) { json ->
			json instanceof JsonObject
			json.message.asString == 'Namaste'
		}
	}

	void 'parses an empty request body as an empty JsonNull'() {
		given:
		request.contentType = APPLICATION_JSON
		request.content = new byte[0]

		when:
		listener.paramsCreated(params)

		then:
		request.getAttribute(CACHED_GSON) instanceof JsonNull
	}

	void 'parses JSON into request parameters'() {
		given:
		request.contentType = APPLICATION_JSON
		request.content = '{"message":"Namaste"}'.getBytes('UTF-8')

		when:
		listener.paramsCreated(params)

		then:
		params.message == 'Namaste'
	}

}
