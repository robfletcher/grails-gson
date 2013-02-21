package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static grails.plugin.gson.test.AlbumController.X_PAGINATION_TOTAL
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static grails.plugin.gson.http.HttpConstants.*
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/1')
class NonStandardIdSpec extends RestEndpointSpec {

	void 'id property is serialized correctly'() {
		given:
		def publication = new Publication(isbn: '1846683025', title: 'Just My Type').save(failOnError: true, flush: true)

		when:
		HttpResponseDecorator response = http.get(path: "publication/$publication.isbn")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType

		and:
		response.data.isbn == publication.isbn
		response.data.title == publication.title

		and:
		response.id == null
	}

}
