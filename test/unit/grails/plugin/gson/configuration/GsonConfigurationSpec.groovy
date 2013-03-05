package grails.plugin.gson.configuration

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.*

@Unroll
@Mock(Person)
class GsonConfigurationSpec extends Specification {

	void setup() {
		defineBeans {
			domainSerializer GrailsDomainSerializer, ref('grailsApplication')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) { bean ->
				bean.singleton = false
				pluginManager = ref('pluginManager')
			}
		}
	}

	void cleanup() {
		grailsApplication.with {
			config.clear()
			configChanged()
		}
	}

	void 'pretty print is configurable'() {
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

	void 'null serialization is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeNulls = serializeNulls
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: null)) == expectedOutput

		where:
		serializeNulls || expectedOutput
		null           || '{}'
		false          || '{}'
		true           || '{"id":null,"name":null}'
	}

	void 'complex map key serialization is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.complexMapKeySerialization = complexMapKeySerialization
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson([(new Person(name: 'Rob')): 'Developer']) == expectedOutput

		where:
		complexMapKeySerialization || expectedOutput
		null                       || '{"Rob":"Developer"}'
		false                      || '{"Rob":"Developer"}'
		true                       || '[[{"name":"Rob"},"Developer"]]'
	}

	void 'HTML escaping is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.escapeHtmlChars = escapeHtmlChars
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson('<div class="foo">foo</div>') == expectedOutput

		where:
		escapeHtmlChars || expectedOutput
		null            || '"\\u003cdiv class\\u003d\\"foo\\"\\u003efoo\\u003c/div\\u003e"'
		true            || '"\\u003cdiv class\\u003d\\"foo\\"\\u003efoo\\u003c/div\\u003e"'
		false           || '"<div class=\\"foo\\">foo</div>"'
	}

	void 'non-executable output is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.generateNonExecutableJson = generateNonExecutableJson
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson('foo') == expectedOutput

		where:
		generateNonExecutableJson || expectedOutput
		null                      || '"foo"'
		false                     || '"foo"'
		true                      || ')]}\'\n"foo"'
	}

	void 'special floating point values cause exceptions if configuration says they should not be handled'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeSpecialFloatingPointValues = serializeSpecialFloatingPointValues
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		when:
		gson.toJson(Double.NaN)

		then:
		thrown(IllegalArgumentException)

		where:
		serializeSpecialFloatingPointValues << [null, false]
	}

	void 'special floating point values do not cause exceptions if configuration says they should be handled'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeSpecialFloatingPointValues = serializeSpecialFloatingPointValues
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		when:
		gson.toJson(Double.NaN)

		then:
		notThrown(IllegalArgumentException)

		where:
		serializeSpecialFloatingPointValues = true
	}

	void 'long serialization policy is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.longSerializationPolicy = longSerializationPolicy
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		and:
		def person = new Person(name: 'Rob').save(failOnError: true)

		expect:
		gson.toJson(person) == expectedOutput

		where:
		longSerializationPolicy         || expectedOutput
		null                            || '{"id":1,"name":"Rob"}'
		LongSerializationPolicy.DEFAULT || '{"id":1,"name":"Rob"}'
		LongSerializationPolicy.STRING  || '{"id":"1","name":"Rob"}'
	}

	void 'field naming policy is configurable'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.fieldNamingPolicy = fieldNamingPolicy
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: 'Rob')) == expectedOutput

		where:
		fieldNamingPolicy                  || expectedOutput
		null                               || '{"name":"Rob"}'
		FieldNamingPolicy.IDENTITY         || '{"name":"Rob"}'
		FieldNamingPolicy.UPPER_CAMEL_CASE || '{"Name":"Rob"}'
	}

}

@Entity
class Person {
	String name

	@Override
	String toString() {
		name
	}
}
