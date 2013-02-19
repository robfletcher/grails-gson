package grails.plugin.gson.test

import grails.plugin.fixtures.FixtureLoader
import groovyx.net.http.RESTClient
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.*

@Unroll
abstract class RestEndpointSpec extends Specification {

	static final BASE_URL = 'http://localhost:8080/'

	@Shared protected RESTClient http = new RESTClient(BASE_URL)
	protected FixtureLoader fixtureLoader = ApplicationHolder.application.mainContext.fixtureLoader

	void cleanup() {
		Album.withNewSession { session ->
			Album.list()*.delete()
			Artist.list()*.delete()
			session.flush()
		}
	}

}
