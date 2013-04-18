package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*

@Mock([SuperHero])
class InheritedPropertySpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	void setup() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		gson = gsonBuilder.create()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [firstName: 'Clark', lastName: 'Kent', superHeroName: 'Superman']
		def json = gson.toJson(data)

		when:
		def sp = gson.fromJson(json, SuperHero)

		then:
		sp.firstName == data.firstName
		sp.lastName == data.lastName
        sp.superHeroName == data.superHeroName
	}

	void 'can deserialize an existing instance'() {
		given:
		def sh1 = new SuperHero(firstName: 'Clark', lastName: 'Kent', superHeroName: 'Superman').save(failOnError: true)

		and:
		def data = [id: sh1.id, superHeroName: 'The Red-Blue Blur']
		def json = gson.toJson(data)

		when:
		def p2 = gson.fromJson(json, SuperHero)

		then:
		p2.firstName == sh1.firstName
		p2.lastName == sh1.lastName
		p2.superHeroName == data.superHeroName
	}

	void 'can serialize an instance'() {
		given:
		def sh = new SuperHero(firstName: 'Clark', lastName: 'Kent', superHeroName: 'Superman').save(failOnError: true)

		expect:
		def json = gson.toJsonTree(sh)
		json.id.asLong == sh.id
		json.firstName.asString == sh.firstName
		json.lastName.asString == sh.lastName
		json.superHeroName.asString == sh.superHeroName
	}

}

@Entity
class Hero {
	String firstName
	String lastName
}

@Entity
class SuperHero extends Hero {
    String superHeroName
}
 
 