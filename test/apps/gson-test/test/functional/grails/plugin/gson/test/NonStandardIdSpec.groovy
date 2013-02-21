package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.JSON
import static grails.plugin.gson.test.AlbumController.X_PAGINATION_TOTAL
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static grails.plugin.gson.http.HttpConstants.*
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/1')
class NonStandardIdSpec extends RestEndpointSpec {

	void 'id property is serialized correctly'() {
		given:
		def publication = Publication.withNewSession {
			new Publication(isbn: '978-1846683022', title: 'Just My Type').save(failOnError: true, flush: true)
		}

		when:
		HttpResponseDecorator response = http.get(path: "publication/$publication.isbn")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType

		and:
		response.data.isbn == publication.isbn
		response.data.title == publication.title

		and:
		response.data.id == null
	}

	void 'id property is deserialized correctly'() {
		given:
		def publication = Publication.withNewSession {
			new Publication(isbn: '978-1846683022', title: 'Just My Type').save(failOnError: true, flush: true)
		}

		and:
		def request = [isbn: publication.isbn, title: 'Just My Type: A Book About Fonts']

		when:
		HttpResponseDecorator response = http.put(path: "publication/$publication.isbn", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType

		and:
		response.data.isbn == publication.isbn
		response.data.title == request.title

		and:
		publication.attach().refresh()
		publication.title == request.title
	}

}
