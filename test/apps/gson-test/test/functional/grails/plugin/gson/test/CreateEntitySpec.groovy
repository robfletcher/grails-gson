package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.SC_CREATED
import static org.apache.http.entity.ContentType.APPLICATION_JSON

class CreateEntitySpec extends RestEndpointSpec {

	void 'save creates a new instance given valid JSON'() {
		given:
		def artist = new Artist(name: 'Best Coast').save(failOnError: true, flush: true)

		and:
		def request = [
				title: 'The Only Place',
				artist: [id: artist.id],
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
		response.data.artist.id == artist.id
		response.data.artist.name == artist.name
		response.data.year == request.year
		response.data.tracks == request.tracks

		and:
		Album.count() == old(Album.count()) + 1
		Artist.count() == old(Artist.count())
	}

	void 'save can create new instances of related domains'() {
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

}
