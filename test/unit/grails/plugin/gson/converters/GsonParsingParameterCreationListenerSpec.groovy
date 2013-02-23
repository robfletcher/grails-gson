package grails.plugin.gson.converters

import com.google.gson.*
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.web.converters.JSONParsingParameterCreationListener
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import spock.lang.*
import static grails.plugin.gson.converters.GSON.CACHED_GSON

@Unroll
@TestMixin(ControllerUnitTestMixin)
class GsonParsingParameterCreationListenerSpec extends Specification {

	private static final String APPLICATION_JSON = GrailsWebUtil.getContentType('application/json', 'UTF-8')

	def gsonBuilder = new GsonBuilder()
	def gsonListener = new GsonParsingParameterCreationListener(gsonBuilder)
	def jsonListener = new JSONParsingParameterCreationListener()

	void 'does nothing if the request content is not JSON'() {
		when:
		gsonListener.paramsCreated(params)

		then:
		request.getAttribute(CACHED_GSON) == null
	}

	void 'parses and caches a JsonElement if request content is JSON'() {
		given:
		request.contentType = APPLICATION_JSON
		request.content = '{"message":"Namaste"}'.getBytes('UTF-8')

		when:
		gsonListener.paramsCreated(params)

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
		gsonListener.paramsCreated(params)

		then:
		request.getAttribute(CACHED_GSON) instanceof JsonNull
	}

	void 'parses #description into request parameters consistently with JSON implementation'() {
		given:
		def jsonListener = new JSONParsingParameterCreationListener()

		and:
		request.contentType = APPLICATION_JSON
		request.content = requestBody.getBytes('UTF-8')
		request.format = 'json'

		and:
		def clonedParams = new GrailsParameterMap(request)

		when:
		gsonListener.paramsCreated(params)
		jsonListener.paramsCreated(clonedParams)

		then:
		params == clonedParams

		where:
		requestBody                                                                                   | description
		'{"message":"Namaste"}'                                                                       | 'simple json object'
		'{"message":{"type":"Greeting","content":"Namaste"}}'                                         | 'nested json object'
		'{"message":[{"type":"Greeting","content":"Namaste"},{"type":"Greeting","content":"O HAI"}]}' | 'json object with nested arrays'
		'{"class":"foo.Greeting","message":"Namaste"}'                                                | 'json with "class" value'
	}

}
