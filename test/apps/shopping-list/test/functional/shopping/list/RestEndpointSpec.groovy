package shopping.list

import groovyx.net.http.*
import spock.lang.*
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.*
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static shopping.list.ItemController.*

@Unroll
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
		response.contentType == APPLICATION_JSON.mimeType
		response.data == []
		response.getFirstHeader(X_PAGINATION_TOTAL).value == '0'
	}

	void 'list returns a JSON array when there is some data'() {
		given:
		def item = new Item(description: 'Gin', quantity: 1, unit: 'bottle').save(failOnError: true, flush: true)

		when:
		HttpResponseDecorator response = http.get(path: 'items')

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.size() == 1
		response.getFirstHeader(X_PAGINATION_TOTAL).value == '1'
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
		response.contentType == APPLICATION_JSON.mimeType
		response.data.description == item.description
		response.data.quantity == item.quantity
		response.data.unit == item.unit
	}

	void '#action returns a 404 given a non-existent id'() {
		when:
		http."$method"(path: 'item/1', requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.message == 'Item not found with id 1'

		where:
		action   | method
		'show'   | 'get'
		'delete' | 'delete'
	}

	void '#action returns a 406 given non-JSON data'() {
		when:
		http."$method"(path: 'item')

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_ACCEPTABLE

		where:
		action   | method
		'save'   | 'post'
		'update' | 'put'
	}

	void 'save returns a 422 given invalid JSON'() {
		when:
		http.post(path: 'item', body: [:], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [description] of class [class shopping.list.Item] cannot be null'
		e.response.data.errors[1] == 'Property [quantity] of class [class shopping.list.Item] cannot be null'
	}

	void 'save creates a new instance given valid JSON'() {
		when:
		def request = [description: 'Gin', quantity: 1, unit: 'bottles']
		HttpResponseDecorator response = http.post(path: 'item', body: request, requestContentType: JSON)

		then:
		response.status == SC_CREATED
		response.contentType == APPLICATION_JSON.mimeType
		response.data.description == request.description
		response.data.quantity == request.quantity
		response.data.units == request.units

		and:
		Item.count() == old(Item.count()) + 1
	}

	void 'update returns a 404 given a non-existent id'() {
		when:
		http.put(path: 'item/1', body: [description: 'Gin', quantity: 1], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.message == 'Item not found with id 1'
	}

	void 'update returns a 422 given invalid JSON'() {
		given:
		def item = new Item(description: 'Gin', quantity: 1, unit: 'bottle').save(failOnError: true, flush: true)

		when:
		http.put(path: "item/$item.id", body: [description: '', quantity: 0], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [description] of class [class shopping.list.Item] cannot be blank'
		e.response.data.errors[1] == 'Property [quantity] of class [class shopping.list.Item] is less than minimum value [1]'
	}

}
