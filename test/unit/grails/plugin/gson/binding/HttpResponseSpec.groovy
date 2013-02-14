package grails.plugin.gson.binding

import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletResponse

import grails.plugin.gson.*
import grails.test.mixin.*

@ConfineMetaClassChanges(HttpServletResponse)
@TestFor(AlbumController)
@Mock(Album)
class HttpResponseSpec extends Specification {

    def controller

    void setup() {
		new ArtefactEnhancer(grailsApplication).enhanceControllers()

        controller = new AlbumController()
    }

    void 'can render using GSON converter'() {
        given:
        def album = new Album(artist: 'David Bowie', title: 'The Rise and Fall of Ziggy Stardust and the Spiders From Mars').save(failOnError: true)

        when:
        controller.index()

        then:
        response.contentAsString == /[{"artist":"David Bowie","title":"The Rise and Fall of Ziggy Stardust and the Spiders From Mars","id":$album.id}]/
    }

}

class AlbumController {
    def index() {
        render Album.list() as GSON
    }
}