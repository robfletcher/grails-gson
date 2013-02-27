package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import spock.lang.Issue
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

	@Issue('https://github.com/robfletcher/grails-gson/issues/15')
	@Issue('https://github.com/robfletcher/grails-gson/issues/24')
	def 'save can create complex sub-graph'() {
		given:
		def request = [
				name: 'David Bowie',
				albums: [
						[
								title: 'Station to Station',
								year: 1976,
								tracks: [
										'Station to Station', 'Golden Years', 'Word on a Wing',
										'TVC 15', 'Stay', 'Wild is the Wind'
								]
						],
						[
								title: 'Low',
								year: 1977,
								tracks: [
										'Speed of Life', 'Breaking Glass', 'What in the World',
										'Sound and Vision', 'Always Crashing in the Same Car',
										'Be My Wife', 'A New Career in a New Town', 'Warszawa',
										'Art Decade', 'Weeping Wall', 'Subterraneans'
								]

						]
				]
		]

		when:
		HttpResponseDecorator response
		try {
			response = http.post(path: 'artist', body: request, requestContentType: JSON)
		} catch(HttpResponseException e) {
			println e.response.data.toString()
			e.printStackTrace()
			response = e.response
		}

		then:
		response.status == SC_CREATED
		response.contentType == APPLICATION_JSON.mimeType
		response.data.name == request.name
		response.data.albums.size() == 2
		response.data.albums.title == request.albums.title
		response.data.albums.year == request.albums.year
		response.data.albums[0].tracks == request.albums[0].tracks
		response.data.albums[1].tracks == request.albums[1].tracks

		and:
		Album.count() == old(Album.count()) + 2
		Artist.count() == old(Artist.count()) + 1
	}

}
