package grails.plugin.gson.test

import groovyx.net.http.HttpResponseDecorator
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.Issue
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.apache.http.entity.ContentType.APPLICATION_JSON

@Issue('https://github.com/robfletcher/grails-gson/issues/26')
class HibernateProxySpec extends RestEndpointSpec {

	def grailsApplication = ApplicationHolder.application
	Album album
	Artist artist

	void setup() {
		def fixture = fixtureLoader.load('albums')
		album = fixture.aThingCalledDivineFits
		artist = fixture.divineFits
	}

	void cleanup() {
		grailsApplication.config.grails.converters.gson.remove('resolveProxies')
		grailsApplication.configChanged()
	}

	void 'many-to-one proxies are resolved by default'() {
		given:
		grailsApplication.config.grails.converters.gson.resolveProxies = true // the default
		grailsApplication.configChanged()

		when:
		HttpResponseDecorator response = http.get(path: "album/$album.id")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == album.title
		response.data.artist.id == album.artist.id
		response.data.artist.name == album.artist.name
	}

	void 'one-to-many proxies are resolved by default'() {
		given:
		grailsApplication.config.grails.converters.gson.resolveProxies = true // the default
		grailsApplication.configChanged()

		when:
		HttpResponseDecorator response = http.get(path: "artist/$artist.id")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.name == artist.name
		response.data.albums.size() == artist.albums.size()
		response.data.albums.title == artist.albums.title
		response.data.albums.year == artist.albums.year
		response.data.albums.tracks == artist.albums.tracks
	}

	void 'many-to-one proxies are not resolved if configuration says they should not be'() {
		given:
		grailsApplication.config.grails.converters.gson.resolveProxies = false
		grailsApplication.configChanged()

		when:
		HttpResponseDecorator response = http.get(path: "album/$album.id")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.title == album.title
		response.data.artist.id == album.artist.id
		response.data.artist.name == null
	}

	void 'one-to-many proxies are not resolved if configuration says they should not be'() {
		given:
		grailsApplication.config.grails.converters.gson.resolveProxies = false
		grailsApplication.configChanged()

		when:
		HttpResponseDecorator response = http.get(path: "artist/$artist.id")

		then:
		response.status == SC_OK
		response.contentType == APPLICATION_JSON.mimeType
		response.data.name == artist.name
		response.data.albums == null
	}

}