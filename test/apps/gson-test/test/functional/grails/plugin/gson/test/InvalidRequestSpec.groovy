package grails.plugin.gson.test

import groovyx.net.http.HttpResponseException
import spock.lang.*
import static grails.plugin.gson.http.HttpConstants.SC_UNPROCESSABLE_ENTITY
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE
import static org.apache.http.entity.ContentType.APPLICATION_JSON

@Unroll
class InvalidRequestSpec extends RestEndpointSpec {

	void '#action returns a 406 given non-JSON data'() {
		when:
		http."$method"(path: 'album')

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
		http.post(path: 'album', body: [:], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [artist] of class [class grails.plugin.gson.test.Album] cannot be null'
		e.response.data.errors[1] == 'Property [title] of class [class grails.plugin.gson.test.Album] cannot be null'
	}

	void 'update returns a 422 given invalid JSON'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		when:
		http.put(path: "album/$album.id", body: [title: ''], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [title] of class [class grails.plugin.gson.test.Album] cannot be blank'
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/20')
	void 'save returns a 422 if an id specified for a nested entity does not exist'() {
		when:
		http.post(path: 'album', body: [title: 'Hunky Dory', artist: [id: Long.MAX_VALUE]], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.data.errors[0] == 'Property [artist] of class [class grails.plugin.gson.test.Album] cannot be null'
	}

	void 'save returns a 422 with details of error for a problem at a nested level in the graph'() {
		when:
		http.post(path: 'artist', body: [name: 'David Bowie', albums: [[title: null]]], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [title] of class [class grails.plugin.gson.test.Album] cannot be null'
	}

	void 'update returns a 422 with details of error for a problem at a nested level in the graph'() {
		given:
		def fixture = fixtureLoader.load('albums')
		def artist = fixture.divineFits
		def album = fixture.aThingCalledDivineFits
		println "id is a ${album.id.getClass().simpleName}"

		when:
		http.put(path: "artist/$artist.id", body: [albums: [[id: album.id, title: null]]], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [title] of class [class grails.plugin.gson.test.Album] cannot be null'
	}

}
