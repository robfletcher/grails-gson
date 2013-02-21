package grails.plugin.gson.test

import groovyx.net.http.*
import spock.lang.Unroll
import static grails.plugin.gson.test.AlbumController.X_PAGINATION_TOTAL
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.*
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static grails.plugin.gson.http.HttpConstants.*

@Unroll
class NoDataSpec extends RestEndpointSpec {

	void 'list returns an empty JSON array when there is no data'() {
		when:
		HttpResponseDecorator response = http.get(path: 'albums')

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data == []
		response.getFirstHeader(X_PAGINATION_TOTAL).value == '0'
	}

	void '#action returns a 404 given a non-existent id'() {
		when:
		http."$method"(path: 'album/1', requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.message == 'Album not found with id 1'

		where:
		action   | method
		'show'   | 'get'
		'delete' | 'delete'
	}

	void 'update returns a 404 given a non-existent id'() {
		when:
		http.put(path: 'album/1', body: [title: 'Mosquito'], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.message == 'Album not found with id 1'
	}

}
