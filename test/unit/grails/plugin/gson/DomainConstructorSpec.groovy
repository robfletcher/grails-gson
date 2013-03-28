package grails.plugin.gson

import com.google.gson.*
import grails.converters.JSON
import grails.plugin.gson.adapters.GrailsDomainDeserializer
import grails.plugin.gson.api.ArtefactEnhancer
import grails.test.mixin.*
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/21')
@TestMixin(ControllerUnitTestMixin)
@Mock(Person)
@Unroll
class DomainConstructorSpec extends Specification {

	@Shared def json = '{"name":"Rob"}'
	@Shared def map = [name: 'Rob']

	void setup() {
		def gsonBuilder = new GsonBuilder()
		def domainDeserializer = new GrailsDomainDeserializer()
		def enhancer = new ArtefactEnhancer(grailsApplication, gsonBuilder, domainDeserializer)
		enhancer.enhanceDomains()
	}

	void 'can construct a domain instance using a #type'() {
		when:
		def instance = new Person(parameter)

		then:
		instance.name == 'Rob'

		where:
		parameter << [
				new JsonParser().parse(json),
				JSON.parse(json),
				map
		]
		type = parameter.getClass().name
	}

	void 'can construct a domain instance using a GrailsParameterMap'() {
		given:
		request.setParameter('name', 'Rob')
		def parameter = new GrailsParameterMap(request)

		when:
		def instance = new Person(parameter)

		then:
		instance.name == 'Rob'
	}

}
