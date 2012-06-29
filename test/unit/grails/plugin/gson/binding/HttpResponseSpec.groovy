package grails.plugin.gson.binding

import grails.test.mixin.Mock
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletResponse

import com.google.gson.*
import grails.plugin.gson.*
import grails.test.mixin.TestFor

@ConfineMetaClassChanges(HttpServletResponse)
@TestFor(AlbumController)
@Mock(Album)
class HttpResponseSpec extends Specification {

	Gson gson
	def controller

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()

		HttpServletResponse.metaClass.render = { JsonElement json ->
			delegate.outputStream << gson.toJson(json)
		}

		controller = new AlbumController()
	}

	void 'can render JSON and so on'() {
		given:
		def response = new GrailsMockHttpServletResponse()

		and:
		def album = new Album(artist: 'David Bowie', title: 'The Rise and Fall of Ziggy Stardust and the Spiders From Mars')

		when:
		controller.render album as JSON

		then:
		response.contentAsString == '{"artist":"David Bowie","title":"The Rise and Fall of Ziggy Stardust and the Spiders From Mars"}'
	}

}

class AlbumController {
	def index() {
		render Album.list() as JSON
	}
}