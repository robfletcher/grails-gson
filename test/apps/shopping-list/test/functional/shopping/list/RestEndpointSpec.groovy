package shopping.list

import groovyx.net.http.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import static javax.servlet.http.HttpServletResponse.SC_OK

class RestEndpointSpec extends Specification {

	static final BASE_URL = 'http://localhost:8080/shopping-list/'

	@Shared RESTClient http = new RESTClient(BASE_URL)

	void cleanup() {
		Item.withNewSession { session ->
			Item.list()*.delete()
			session.flush()
		}
	}

	void 'list returns an empty JSON array when there is no data'() {
		when:
		HttpResponseDecorator response = http.get(path: 'items')

		then:
		response.status == SC_OK
		response.contentType == 'application/json'
		response.data == []
	}

	void 'list returns a JSON array when there is some data'() {
		given:
		def item = new Item(description: 'Gin', quantity: 1, unit: 'bottle').save(failOnError: true, flush: true)

		when:
		HttpResponseDecorator response = http.get(path: 'items')

		then:
		response.status == SC_OK
		response.contentType == 'application/json'
		response.data.size() == 1
		response.data[0].description == item.description
		response.data[0].quantity == item.quantity
		response.data[0].unit == item.unit
	}

	void 'show returns a JSON object when there is some data'() {
		given:
		def item = new Item(description: 'Gin', quantity: 1, unit: 'bottle').save(failOnError: true, flush: true)

		when:
		HttpResponseDecorator response = http.get(path: "item/$item.id")

		then:
		response.status == SC_OK
		response.contentType == 'application/json'
		response.data.description == item.description
		response.data.quantity == item.quantity
		response.data.unit == item.unit
	}

	void 'show returns a 404 given a non-existent id'() {
		when:
		HttpResponseDecorator response = http.get(path: "item/1")

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND
		e.response.contentType == 'application/json'
		e.response.data.message == 'Item not found with id 1'
	}

}
