package grails.plugin.gson.binding

import com.google.gson.JsonElement
import grails.persistence.Entity
import grails.plugin.gson.ArtefactEnhancer
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest

@ConfineMetaClassChanges(HttpServletRequest)
@Mock(Album)
class RequestBodySpec extends Specification {

	void setup() {
		def enhancer = new ArtefactEnhancer(grailsApplication)
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()
    }

    void 'can get JSON data from request'() {
        given:
        def request = new GrailsMockHttpServletRequest()
        request.content = '{"artist":"Metric","title":"Synthetica"}'.bytes

        expect:
        JsonElement.isAssignableFrom(request.JSON.getClass())
        request.JSON.artist.getAsString() == 'Metric'
        request.JSON.title.getAsString() == 'Synthetica'
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
        album.properties = request.JSON

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