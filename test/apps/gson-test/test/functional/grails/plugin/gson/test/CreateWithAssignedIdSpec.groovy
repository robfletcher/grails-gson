package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.JSON
import static grails.plugin.gson.test.AlbumController.X_PAGINATION_TOTAL
import static javax.servlet.http.HttpServletResponse.SC_CREATED
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static grails.plugin.gson.http.HttpConstants.*
import spock.lang.*

class CreateWithAssignedIdSpec extends RestEndpointSpec {

	void 'id property is serialized correctly'() {
		given:
		def request = [isbn: '978-1846683022', title: 'Just My Type']

		when:
		HttpResponseDecorator response = http.post(path: 'publication', body: request, requestContentType: JSON)

		then:
		response.status == SC_CREATED
		response.contentType == APPLICATION_JSON.mimeType

		and:
		response.data.isbn == request.isbn
		response.data.title == request.title

		and:
		Publication.countByIsbn(request.isbn) == 1
	}

}
