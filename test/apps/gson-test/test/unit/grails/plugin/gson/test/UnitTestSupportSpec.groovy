package grails.plugin.gson.test

import com.google.gson.Gson
import grails.converters.JSON
import grails.plugin.gson.converters.GSON
import grails.test.mixin.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_CREATED

@Issue('https://github.com/robfletcher/grails-gson/issues/28')
@TestFor(AlbumController)
@TestMixin(GsonUnitTestMixin)
@Mock([Artist, Album])
@Unroll
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
		request.GSON = "{\"title\":\"The Next Day\",\"artist\":{\"id\":$artist.id}}"

		when:
		controller.save()

		then:
		response.status == SC_CREATED

		and:
		Album.countByTitle('The Next Day') == 1
		def album = Album.findByTitle('The Next Day')
		album.artist.name == artist.name
	}

	@Shared def data = [title: "The Next Day", artist: [name: "David Bowie"]]

	void 'can assign #type to request.GSON'() {
		when:
		request.GSON = body

		then:
		request.GSON.title.asString == data.title
		request.GSON.artist.name.asString == data.artist.name

		where:
		body << [
				new Gson().toJson(data),
				data,
				new Gson().toJsonTree(data),
				data as GSON,
				data as JSON
		]

		type = body.getClass().simpleName
	}

}
