package grails.plugin.gson.test

import grails.test.mixin.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_CREATED

@Issue('https://github.com/robfletcher/grails-gson/issues/28')
@TestFor(AlbumController)
@TestMixin(GsonUnitTestMixin)
@Mock([Artist, Album])
class UnitTestSupportSpec extends Specification {

	Artist artist

	void setup() {
		artist = new Artist(name: 'David Bowie').save(failOnError: true)
	}

	void 'can list mocked domain instances'() {
		given:
		for (title in ['Low', '"Heroes"', 'Lodger'])
			new Album(artist: artist, title: title).save(failOnError: true)

		when:
		controller.list(10)

		then:
		with(response.GSON) {
			it.size() == 3
			it*.title.asString == ['Low', '"Heroes"', 'Lodger']
			it*.artist.name.asString.unique() == [artist.name]
			it*.id.asLong == Album.list().id
		}
	}

	void 'can create a new mocked domain instance'() {
		given:
		request.contentType = 'application/json'
		request.content = "{\"title\":\"The Next Day\",\"artist\":{\"id\":$artist.id}}".bytes

		when:
		controller.save()

		then:
		response.status == SC_CREATED

		and:
		Album.countByTitle('The Next Day') == 1
		def album = Album.findByTitle('The Next Day')
		album.artist.name == artist.name
	}

}
