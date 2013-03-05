package grails.plugin.gson

import com.google.gson.*
import grails.converters.JSON
import grails.plugin.gson.adapters.GrailsDomainDeserializer
import grails.plugin.gson.metaclass.ArtefactEnhancer
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/21')
@TestMixin(GrailsUnitTestMixin)
@Mock(Person)
@Unroll
class DomainConstructorSpec extends Specification {

	@Shared def json = '{"name":"Rob"}'

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
				JSON.parse(json)
		]
		type = parameter.getClass().name

	}

}
