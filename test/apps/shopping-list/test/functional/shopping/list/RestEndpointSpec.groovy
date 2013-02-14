package shopping.list

import groovyx.net.http.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_OK

class RestEndpointSpec extends Specification {

	static final BASE_URL = 'http://localhost:8080/shopping-list/item'

	@Shared RESTClient http = new RESTClient(BASE_URL)

	void 'returns an empty JSON array when there is no data'() {
		when:
		HttpResponseDecorator response = http.get(path: '/')

		then:
		response.status == SC_OK
		response.contentType == 'application/json'
		response.data == []
	}

}
