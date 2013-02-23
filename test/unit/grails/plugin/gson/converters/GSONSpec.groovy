package grails.plugin.gson.converters

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.*

@Unroll
class GSONSpec extends Specification {

	void 'content type "#contentType" #assertion JSON'() {
		given:
		def request = new MockHttpServletRequest()
		request.contentType = contentType

		expect:
		GSON.isJson(request) == isJson

		where:
		contentType                      | isJson
		'application/json'               | true
		'text/json'                      | true
		'text/json;charset=UTF-8'        | true
		'application/json;charset=UTF-8' | true
		'text/plain'                     | false

		assertion = isJson ? 'is' : 'is not'
	}

}
