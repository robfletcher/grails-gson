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
		deleteAll Album
		deleteAll Artist
		deleteAll Publication
	}

	private void deleteAll(Class type) {
		type.withNewSession { session ->
			type.list()*.delete()
			session.flush()
		}
	}

}
