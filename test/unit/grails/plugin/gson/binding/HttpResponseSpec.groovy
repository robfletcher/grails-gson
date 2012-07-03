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

    void setupSpec() {
        defineBeans {
            gsonFactory(GsonFactory)
        }
    }

    void setup() {
        def factory = applicationContext.getBean('gsonFactory')
        gson = factory.createGson()

        for (controller in grailsApplication.controllerClasses) {
            controller.clazz.metaClass.render = { JSON json ->
                json.render delegate.response
            }
        }

        controller = new AlbumController()
    }

    void 'can render JSON and so on'() {
        given:
        new Album(artist: 'David Bowie', title: 'The Rise and Fall of Ziggy Stardust and the Spiders From Mars').save(failOnError: true)

        when:
        controller.index()

        then:
        response.contentAsString == '[{"artist":"David Bowie","title":"The Rise and Fall of Ziggy Stardust and the Spiders From Mars"}]'
    }

}

class AlbumController {
    def index() {
        render Album.list() as JSON
    }
}