package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static grails.plugin.gson.test.AlbumController.X_PAGINATION_TOTAL
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.apache.http.entity.ContentType.APPLICATION_JSON

class QueryEntitySpec extends RestEndpointSpec {

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

}
