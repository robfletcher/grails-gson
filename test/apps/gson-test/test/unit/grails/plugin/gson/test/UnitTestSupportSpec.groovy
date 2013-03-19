package grails.plugin.gson.test

import javax.servlet.http.HttpServletResponse
import com.google.gson.*
import grails.plugin.gson.adapters.*
import grails.plugin.gson.metaclass.ArtefactEnhancer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_CREATED

@Issue('https://github.com/robfletcher/grails-gson/issues/28')
@TestFor(AlbumController)
@Mock([Artist, Album])
class UnitTestSupportSpec extends Specification {

	Gson gson
	Artist artist

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
		def gsonBuilder = applicationContext.gsonBuilder
		gson = gsonBuilder.create()

		def deserializer = applicationContext.domainDeserializer
		def enhancer = new ArtefactEnhancer(grailsApplication, gsonBuilder, deserializer)
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()

		def parser = new JsonParser()
		HttpServletResponse.metaClass.getContentAsJson = {->
			parser.parse delegate.contentAsString
		}

		artist = new Artist(name: 'David Bowie').save(failOnError: true)
	}

	void 'can list mocked domain instances'() {
		given:
		for (title in ['Low', '"Heroes"', 'Lodger'])
			new Album(artist: artist, title: title).save(failOnError: true)

		when:
		controller.list(10)

		then:
		with(response.contentAsJson) {
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
