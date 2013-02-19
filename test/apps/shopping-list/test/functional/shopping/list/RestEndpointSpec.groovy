package shopping.list

import groovyx.net.http.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.*
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.*
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static shopping.list.AlbumController.*

@Unroll
class RestEndpointSpec extends Specification {

	static final BASE_URL = 'http://localhost:8080/shopping-list/'

	@Shared RESTClient http = new RESTClient(BASE_URL)
	def fixtureLoader = ApplicationHolder.application.mainContext.fixtureLoader

	void cleanup() {
		Album.withNewSession { session ->
			Album.list()*.delete()
			Artist.list()*.delete()
			session.flush()
		}
	}

	void 'list returns an empty JSON array when there is no data'() {
		when:
		HttpResponseDecorator response = http.get(path: 'albums')

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data == []
		response.getFirstHeader(X_PAGINATION_TOTAL).value == '0'
	}

	void 'list returns a JSON array when there is some data'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		when:
		HttpResponseDecorator response = http.get(path: 'albums')

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.size() == 1
		response.getFirstHeader(X_PAGINATION_TOTAL).value == '1'
		response.data[0].title == album.title
		response.data[0].artist.id == album.artist.id
		response.data[0].artist.name == album.artist.name
		response.data[0].year == album.year
		response.data[0].tracks == album.tracks
	}

	void 'show returns a JSON object when there is some data'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		when:
		HttpResponseDecorator response = http.get(path: "album/$album.id")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == album.title
		response.data.artist.id == album.artist.id
		response.data.artist.name == album.artist.name
		response.data.year == album.year
		response.data.tracks == album.tracks
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
		e.response.data.errors[0] == 'Property [artist] of class [class shopping.list.Album] cannot be null'
		e.response.data.errors[1] == 'Property [title] of class [class shopping.list.Album] cannot be null'
	}

	void 'save creates a new instance given valid JSON'() {
		given:
		def request = [
				title: 'The Only Place',
				artist: [name: 'Best Coast'],
				year: 2012,
				tracks: [
						"The Only Place",
						"Why I Cry",
						"Last Year",
						"My Life",
						"No One Like You",
						"How They Want Me to Be",
						"Better Girl",
						"Do You Love Me Like You Used To",
						"Dreaming My Life Away",
						"Let's Go Home",
						"Up All Night"
				]
		]

		when:
		HttpResponseDecorator response = http.post(path: 'album', body: request, requestContentType: JSON)

		then:
		response.status == SC_CREATED
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == request.title
		response.data.artist.name == request.artist.name
		response.data.year == request.year
		response.data.tracks == request.tracks

		and:
		Album.count() == old(Album.count()) + 1
		Artist.count() == old(Artist.count()) + 1
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

	void 'update returns a 422 given invalid JSON'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		when:
		http.put(path: "album/$album.id", body: [title: ''], requestContentType: JSON)

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_UNPROCESSABLE_ENTITY
		e.response.contentType == APPLICATION_JSON.mimeType
		e.response.data.errors[0] == 'Property [title] of class [class shopping.list.Album] cannot be blank'
	}

	// TODO: should reject invalid data deep in graph

	void 'update succeeds given valid JSON'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
				title: 'Sound Kapital',
				artist: [name: 'Handsome Furs'],
				year: 2011,
				tracks: [
						"When I Get Back",
						"Damage",
						"Bury Me Standing",
						"Memories of the Future",
						"Serve the People",
						"What About Us",
						"Repatriated",
						"Cheap Music",
						"No Feelings"
				]
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == request.title
		response.data.artist.name == request.artist.name
		response.data.year == request.year
		response.data.tracks == request.tracks

		and:
		Album.count() == old(Album.count())
		Artist.count() == old(Artist.count())

		and:
		album.refresh()
		album.title == request.title
		album.artist.name == request.artist.name
		album.year == request.year
		album.tracks == request.tracks
	}

}
