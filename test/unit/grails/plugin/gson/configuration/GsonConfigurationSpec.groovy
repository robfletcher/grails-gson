package grails.plugin.gson.configuration

import java.text.*
import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*
import static java.text.DateFormat.*
import static java.util.Locale.US

@Unroll
@Mock([Person, UnversionedPerson])
class GsonConfigurationSpec extends Specification {

	void setup() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
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

	void 'output is #outputStyle when grails.converters.default.pretty.print is #defaultConfig and pretty.print is #gsonConverterConfig'() {
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

	void 'null properties are #outputStyle when serializeNulls is #configValue'() {
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

	void 'complex map keys are serialized as a #outputStyle when complexMapKeySerialization is #configValue'() {
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

	void 'HTML characters are serialized #outputStyle when escapeHtmlChars is #configValue'() {
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

	void 'serialization output is #outputStyle when generateNonExecutableJson is #configValue'() {
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

	void 'long values are serialized as a #outputStyle when longSerializationPolicy is #configValue'() {
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
			config.grails.converters.gson.fieldNamingPolicy = configValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: 'Rob')) == expectedOutput

		where:
		configValue                        | expectedOutput
		null                               | '{"name":"Rob"}'
		FieldNamingPolicy.IDENTITY         | '{"name":"Rob"}'
		FieldNamingPolicy.UPPER_CAMEL_CASE | '{"Name":"Rob"}'
	}

	void 'date value is output as #expectedOutput when datePattern is #datePatternConfigValue, dateStyle is #dateStyleConfigValue and timeStyle is #timeStyleConfigValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.datePattern = datePatternConfigValue
			config.grails.converters.gson.dateStyle = dateStyleConfigValue
			config.grails.converters.gson.timeStyle = timeStyleConfigValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(value) == expectedOutput

		where:
		datePatternConfigValue | dateStyleConfigValue | timeStyleConfigValue | expectedOutputFormat
		null                   | null                 | null                 | DateFormat.getDateTimeInstance(DEFAULT, DEFAULT, US)
		null                   | SHORT                | null                 | DateFormat.getDateTimeInstance(DEFAULT, DEFAULT, US)
		null                   | LONG                 | SHORT                | DateFormat.getDateTimeInstance(LONG, SHORT, US)
		'yyyy-MM-dd HH:mm:ss'  | null                 | null                 | new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')

		value = new Date()
		expectedOutput = "\"${expectedOutputFormat.format(value)}\""
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/4')
	void 'serialized object #includes class element when domain.include.class is #defaultConfigValue and gson.domain.include.class is #gsonConfigValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.domain.include.class = defaultConfigValue
			config.grails.converters.gson.domain.include.class = gsonConfigValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		expect:
		gson.toJson(new Person(name: 'Rob')) == expectedOutput

		where:
		defaultConfigValue | gsonConfigValue | shouldOutputClass
		null               | null            | false
		false              | false           | false
		true               | false           | false
		true               | null            | true
		false              | true            | true

		expectedOutput = shouldOutputClass ? '{"class":"grails.plugin.gson.configuration.Person","name":"Rob"}' : '{"name":"Rob"}'
		includes = shouldOutputClass ? 'includes' : 'does not include'
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/3')
	void 'serialized object #includes version element when domain.include.version is #defaultConfigValue and gson.domain.include.version is #gsonConfigValue'() {
		given:
		grailsApplication.with {
			config.grails.converters.domain.include.version = defaultConfigValue
			config.grails.converters.gson.domain.include.version = gsonConfigValue
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		and:
		def value = new Person(name: 'Rob').save(failOnError: true, flush: true)

		expect:
		gson.toJson(value) ==~ expectedOutput

		where:
		defaultConfigValue | gsonConfigValue | shouldOutputVersion
		null               | null            | false
		false              | false           | false
		true               | false           | false
		true               | null            | true
		false              | true            | true

		expectedOutput = shouldOutputVersion ? /\{"id":\d+,"version":0,"name":"Rob"\}/ : /\{"id":\d+,"name":"Rob"\}/
		includes = shouldOutputVersion ? 'includes' : 'does not include'
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/3')
	void 'serialized object does not include version element when gson.domain.include.version is true but class is unversioned'() {
		given:
		grailsApplication.with {
			config.grails.converters.gson.domain.include.version = true
			configChanged()
		}

		and:
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def gson = gsonBuilder.create()

		and:
		def value = new UnversionedPerson(name: 'Rob').save(failOnError: true, flush: true)

		expect:
		gson.toJson(value) ==~ /\{"id":\d+,"name":"Rob"\}/
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

@Entity
class UnversionedPerson {
	String name

	@Override
	String toString() {
		name
	}

	static mapping = {
		version false
	}
}
