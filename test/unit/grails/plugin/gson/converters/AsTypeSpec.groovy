package grails.plugin.gson.converters

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.plugins.converters.ConvertersPluginSupport
import spock.lang.*

@Unroll
@TestMixin(GrailsUnitTestMixin)
class AsTypeSpec extends Specification {

	void setup() {
		ConvertersPluginSupport.enhanceApplication(grailsApplication, applicationContext)
		ApplicationHolder.application = grailsApplication
		PluginManagerHolder.pluginManager = applicationContext.pluginManager
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
