package grails.plugin.gson.binding

import grails.persistence.Entity
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest

import com.google.gson.*

@ConfineMetaClassChanges(HttpServletRequest)
@Mock(Album)
class RequestBodySpec extends Specification {

    void setup() {
        GrailsMockHttpServletRequest.metaClass.getJSON = { ->
            new JsonParser().parse(delegate.reader)
        }
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

    void 'can bind request json direct to domain class'() {
        given:
        def request = new GrailsMockHttpServletRequest()
        request.content = '{"artist":"Metric","title":"Synthetica"}'.bytes

        when:
        def album = new Album(request.JSON)

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