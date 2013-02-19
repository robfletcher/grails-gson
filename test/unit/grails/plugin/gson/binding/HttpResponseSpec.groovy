package grails.plugin.gson.binding

import javax.servlet.http.HttpServletResponse
import grails.plugin.gson.converters.GSON
import grails.plugin.gson.metaclass.ArtefactEnhancer
import grails.test.mixin.*
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import spock.lang.*
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(HttpServletResponse)
@TestFor(AlbumController)
@Mock(Album)
class HttpResponseSpec extends Specification {

    void setup() {
		PluginManagerHolder.pluginManager = applicationContext.pluginManager
		new ArtefactEnhancer(grailsApplication, applicationContext.pluginManager).enhanceControllers()
    }

    void 'can render a domain instance list using GSON converter'() {
        given:
        def album = new Album(artist: 'David Bowie', title: 'The Rise and Fall of Ziggy Stardust and the Spiders From Mars').save(failOnError: true)

        when:
        controller.index()

        then:
        response.contentAsString == /[{"artist":"David Bowie","title":"The Rise and Fall of Ziggy Stardust and the Spiders From Mars","id":$album.id}]/
    }

	@Issue('https://github.com/robfletcher/grails-gson/issues/9')
    void 'can render a simple map using GSON converter'() {
        when:
        controller.error()

        then:
        response.contentAsString == /{"error":"o noes"}/
    }

    void 'can render a simple list using GSON converter'() {
        when:
        controller.errors()

        then:
        response.contentAsString == /[{"error":"o noes"},{"error":"sworded"}]/
    }

}

class AlbumController {
    def index() {
        render Album.list() as GSON
    }

	def error() {
		def message = [error: 'o noes']
		render message as GSON
	}

	def errors() {
		def message = [[error: 'o noes'], [error: 'sworded']]
		render message as GSON
	}
}