package grails.plugin.gson.configuration

import com.google.gson.GsonBuilder
import grails.plugin.gson.Person
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.*

@Unroll
@Mock(Person)
class GsonConfigurationSpec extends Specification {

	void setupSpec() {
		defineBeans {
			domainSerializer GrailsDomainSerializer, ref('grailsApplication')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) { bean ->
				bean.singleton = false
				pluginManager = ref('pluginManager')
			}
		}
	}

	void 'pretty print can be configured on'() {
		given:
		grailsApplication.with {
			config.grails.converters.default.pretty.print = defaultConfig
			config.grails.converters.json.pretty.print = jsonConverterConfig
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: 'Rob')) == expectedOutput

		where:
		defaultConfig | jsonConverterConfig || expectedOutput
		null          | null                || '{"name":"Rob"}'
		true          | null                || '{\n  "name": "Rob"\n}'
		false         | true                || '{\n  "name": "Rob"\n}'
	}

}
