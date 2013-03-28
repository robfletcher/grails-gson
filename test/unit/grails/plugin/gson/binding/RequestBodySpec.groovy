package grails.plugin.gson.binding

import javax.servlet.http.HttpServletRequest
import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.api.ArtefactEnhancer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(HttpServletRequest)
@Mock(Album)
class RequestBodySpec extends Specification {

	void setupSpec() {
		defineBeans {
			domainSerializer GrailsDomainSerializer, ref('grailsApplication')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	void setup() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def domainDeserializer = applicationContext.getBean('domainDeserializer', GrailsDomainDeserializer)
		def enhancer = new ArtefactEnhancer(grailsApplication, gsonBuilder, domainDeserializer)
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()
    }

    void 'can get JSON data from request'() {
        given:
        def request = new GrailsMockHttpServletRequest()
        request.content = '{"artist":"Metric","title":"Synthetica"}'.bytes

        expect:
        JsonElement.isAssignableFrom(request.GSON.getClass())
        request.GSON.artist.getAsString() == 'Metric'
        request.GSON.title.getAsString() == 'Synthetica'
    }

    void 'can bind request json direct to new domain class'() {
        given:
        def request = new GrailsMockHttpServletRequest()
        request.content = '{"artist":"Metric","title":"Synthetica"}'.bytes

        when:
        def album = new Album(request.JSON)

        then:
        album.artist == 'Metric'
        album.title == 'Synthetica'
    }

    void 'can bind request json direct to existing domain class'() {
        given:
        def request = new GrailsMockHttpServletRequest()
        request.content = '{"artist":"Metric","title":"Synthetica"}'.bytes

		and:
		def album = new Album()

        when:
        album.properties = request.GSON

        then:
        album.artist == 'Metric'
        album.title == 'Synthetica'
    }

}

@Entity
class Album {
    String artist
    String title
}