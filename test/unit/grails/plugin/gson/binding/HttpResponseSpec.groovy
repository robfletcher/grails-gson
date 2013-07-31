package grails.plugin.gson.binding

import javax.servlet.http.HttpServletResponse
import com.google.gson.GsonBuilder
import grails.plugin.gson.adapters.*
import grails.plugin.gson.converters.GSON
import grails.plugin.gson.api.ArtefactEnhancer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.*
import spock.lang.*
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(HttpServletResponse)
@TestFor(AlbumController)
@Mock(Album)
class HttpResponseSpec extends Specification {

	void setupSpec() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	void setup() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def domainDeserializer = applicationContext.getBean('domainDeserializer', GrailsDomainDeserializer)
		new ArtefactEnhancer(grailsApplication, gsonBuilder, domainDeserializer).enhanceControllers()
    }

    void 'can render a domain instance list using GSON converter'() {
        given:
        def album = new Album(artist: 'David Bowie', title: 'The Rise and Fall of Ziggy Stardust and the Spiders From Mars').save(failOnError: true)

        when:
        controller.index()

        then:
        response.contentAsString == /[{"id":$album.id,"artist":"David Bowie","title":"The Rise and Fall of Ziggy Stardust and the Spiders From Mars"}]/
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