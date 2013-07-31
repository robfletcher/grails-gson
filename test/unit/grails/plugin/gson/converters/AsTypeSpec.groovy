package grails.plugin.gson.converters

import com.google.gson.GsonBuilder
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.plugins.converters.ConvertersPluginSupport
import spock.lang.*

@Unroll
@TestMixin(GrailsUnitTestMixin)
class AsTypeSpec extends Specification {

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
		ConvertersPluginSupport.enhanceApplication(grailsApplication, applicationContext)
	}

	void 'can cast a #type to GSON'() {
		expect:
		toJsonString(o) == expected

		where:
		o                              | expected                          | type
		['a', 'b', 'c']                | '["a","b","c"]'                   | 'List<String>'
		[a: 'a', b: 'b', c: 'c']       | '{"a":"a","b":"b","c":"c"}'       | 'Map<String, String>'
		[[x: 'a'], [x: 'b'], [x: 'c']] | '[{"x":"a"},{"x":"b"},{"x":"c"}]' | 'List<Map<String,String>>'
	}

	private String toJsonString(o) {
		def writer = new StringWriter()
		(o as GSON).render(writer)
		writer.toString()
	}

}
