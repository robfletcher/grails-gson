package grails.plugin.gson.converter

import grails.plugin.gson.GSON
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class AsTypeSpec extends Specification {

	void 'can cast a List<String> to GSON'() {
		given:
		def o = ['a', 'b', 'c']
		def gson = o as GSON

		expect:
		gson instanceof GSON
		gson[0] == 'a'
	}

	void 'can cast a List<Map> to GSON'() {
		given:
		def o = [[x: 'a'], [x: 'b'], [x: 'c']]
		def gson = o as GSON

		expect:
		gson instanceof GSON
		gson[0].x == 'a'
	}

}
