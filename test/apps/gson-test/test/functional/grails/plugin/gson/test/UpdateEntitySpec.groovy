package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.apache.http.entity.ContentType.APPLICATION_JSON

class UpdateEntitySpec extends RestEndpointSpec {

	void 'update can change top level fields of object'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
				title: 'Sound Kapital',
				year: 2011
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == request.title
		response.data.year == request.year

		and:
		Album.count() == old(Album.count())

		and:
		album.refresh()
		album.title == request.title
		album.year == request.year
	}

	void 'update does not affect fields not included in request'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
				year: 2011
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == album.title
		response.data.year == request.year
	}

	void 'update can change contents of a simple collection'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
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
		response.data.tracks == request.tracks

		and:
		album.refresh()
		album.tracks == request.tracks
	}

	void 'update can change property of a relationship'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
				artist: [id: album.artist.id, name: 'Handsome Furs']
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.artist.id == album.artist.id
		response.data.artist.name == request.artist.name

		and:
		Artist.count() == old(Artist.count())

		and:
		album.refresh()
		album.artist.name == request.artist.name
	}

	void 'update can add new elements to a collection'() {
		given:
		def artist = new Artist(name: 'David Bowie').save(failOnError: true, flush: true)

		and:
		def request = [
		        albums: [
		                [title: 'David Bowie'],
		                [title: 'Space Oddity'],
		                [title: 'The Man Who Sold The World']
		        ]
		]

		when:
		HttpResponseDecorator response = http.put(path: "artist/$artist.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.albums.size() == request.albums.size()
		response.data.albums.title == request.albums.title

		and:
		Artist.count() == old(Artist.count())
		Album.count() == old(Album.count()) + request.albums.size()

		and:
		artist.refresh()
		artist.albums.size() == request.albums.size()
		artist.albums.title == request.albums.title
	}

	void 'update can bind a different instance to a relationship'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits
		def artist = new Artist(name: 'Handsome Furs').save(failOnError: true, flush: true)

		and:
		def request = [
				artist: [id: artist.id]
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.artist.id == artist.id
		response.data.artist.name == artist.name

		and:
		Artist.count() == old(Artist.count())

		and:
		album.refresh()
		album.artist.id == artist.id
		album.artist.name == artist.name
	}

	void 'update can bind a new instance to a relationship'() {
		given:
		def album = fixtureLoader.load('albums').aThingCalledDivineFits

		and:
		def request = [
				artist: [name: 'Handsome Furs']
		]

		when:
		HttpResponseDecorator response = http.put(path: "album/$album.id", body: request, requestContentType: JSON)

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.artist.name == request.artist.name

		and:
		Artist.count() == old(Artist.count()) + 1

		and:
		album.refresh()
		album.artist.name == request.artist.name
	}

}
