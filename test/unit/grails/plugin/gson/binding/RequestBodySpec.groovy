package grails.plugin.gson.binding

import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.web.binding.DataBindingUtils
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest

import com.google.gson.*

@ConfineMetaClassChanges(HttpServletRequest)
@Mock(Album)
class RequestBodySpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()

        GrailsMockHttpServletRequest.metaClass.getJSON = { ->
            new JsonParser().parse(delegate.reader)
        }

		for (domainClass in grailsApplication.domainClasses) {
			domainClass.clazz.metaClass.constructor = { JsonElement json ->
				gson.fromJson(json, delegate)
			}
			domainClass.clazz.metaClass.setProperties = { JsonElement json ->
				DataBindingUtils.bindObjectToDomainInstance(domainClass, delegate, gson.fromJson(json, Map))
			}
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