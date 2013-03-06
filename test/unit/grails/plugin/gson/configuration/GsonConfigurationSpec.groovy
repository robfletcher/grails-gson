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

	void 'output is #outputStyle when grails.converters.default.pretty.print is #defaultConfig and grails.converters.gson.pretty.print is #gsonConverterConfig'() {
		given:
		grailsApplication.with {
			config.grails.converters.default.pretty.print = defaultConfig
			config.grails.converters.gson.pretty.print = gsonConverterConfig
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: 'Rob')) == expectedOutput

		where:
		defaultConfig | gsonConverterConfig | prettyPrinted
		null          | null                | false
		true          | null                | true
		false         | true                | true

		expectedOutput = prettyPrinted ? '{\n  "name": "Rob"\n}' : '{"name":"Rob"}'
		outputStyle = prettyPrinted ? 'pretty printed' : 'not pretty printed'
	}

	void 'null properties are #outputStyle when grails.converters.gson.serializeNulls is #configValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeNulls = configValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: null)) == expectedOutput

		where:
		configValue | nullsOutput
		false       | false
		null        | false
		true        | true

		expectedOutput = nullsOutput ? '{"id":null,"name":null}' : '{}'
		outputStyle = nullsOutput ? 'output' : 'not output'
	}

	void 'complex map keys are serialized as a #outputStyle when grails.converters.gson.complexMapKeySerialization is #configValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.complexMapKeySerialization = configValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson([(new Person(name: 'Rob')): 'Developer']) == expectedOutput

		where:
		configValue | complexKeys
		null        | false
		false       | false
		true        | true

		expectedOutput = complexKeys ? '[[{"name":"Rob"},"Developer"]]' : '{"Rob":"Developer"}'
		outputStyle = complexKeys ? 'JSON object' : 'string'
	}

	void 'HTML characters are serialized #outputStyle when grails.converters.gson.escapeHtmlChars is #configValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.escapeHtmlChars = configValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson('<div class="foo">foo</div>') == expectedOutput

		where:
		configValue | htmlEscaped
		null        | true
		true        | true
		false       | false

		expectedOutput = htmlEscaped ? '"\\u003cdiv class\\u003d\\"foo\\"\\u003efoo\\u003c/div\\u003e"' : '"<div class=\\"foo\\">foo</div>"'
		outputStyle = htmlEscaped ? 'escaped' : 'unescaped'
	}

	void 'serialization output is #outputStyle when grails.converters.gson.generateNonExecutableJson is #configValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.generateNonExecutableJson = configValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson('foo') == expectedOutput

		where:
		configValue | outputPrefixed
		null        | false
		false       | false
		true        | true

		expectedOutput = outputPrefixed ? ')]}\'\n"foo"' : '"foo"'
		outputStyle = outputPrefixed ? 'prefixed' : 'not prefixed'
	}

	void 'special floating point values cause exceptions if configuration says they should not be handled'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeSpecialFloatingPointValues = configValue
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
		configValue << [null, false]
	}

	void 'special floating point values do not cause exceptions if configuration says they should be handled'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.serializeSpecialFloatingPointValues = configValue
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
		configValue = true
	}

	void 'long values are serialized as a #outputStyle when grails.converters.gson.longSerializationPolicy is #configValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.longSerializationPolicy = configValue
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
		configValue                     | numeric
		null                            | true
		LongSerializationPolicy.DEFAULT | true
		LongSerializationPolicy.STRING  | false

		outputStyle = numeric ? 'number' : 'string'
		expectedOutput = numeric ? '{"id":1,"name":"Rob"}' : '{"id":"1","name":"Rob"}'
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
		fieldNamingPolicy                  | expectedOutput
		null                               | '{"name":"Rob"}'
		FieldNamingPolicy.IDENTITY         | '{"name":"Rob"}'
		FieldNamingPolicy.UPPER_CAMEL_CASE | '{"Name":"Rob"}'
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
