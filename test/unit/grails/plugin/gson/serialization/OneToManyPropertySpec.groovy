package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Boy, Pet])
class OneToManyPropertySpec extends Specification {

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
		def data = [
			name: 'Alex',
			age: 3,
			pets: [
				[name: 'Goldie', species: 'Goldfish'],
				[name: 'Dottie', species: 'Goldfish']
			]
		]
		def json = gson.toJson(data)

		when:
		def boy = gson.fromJson(json, Boy)

		then:
		boy.name == data.name
		boy.age == data.age
		boy.pets.size() == 2
		boy.pets*.getClass().every { it == Pet }
		['Goldie', 'Dottie'].every { it in boy.pets.name }
		boy.pets.every { it.species == 'Goldfish' }
	}

	void 'can deserialize an existing instance'() {
		given:
		def pet1 = new Pet(name: 'Button', species: 'Goldfish').save(failOnError: true)
		def boy1 = new Boy(name: 'Alex', age: 2, pets: [pet1]).save(failOnError: true)

		and:
		def data = [
			id: boy1.id,
			age: 3,
			pets: [
				[id: pet1.id, name: 'Dottie'],
				[name: 'Goldie', species: 'Goldfish']
			]
		]
		def json = gson.toJson(data)

		when:
		def boy2 = gson.fromJson(json, Boy)

		then:
		boy2.id == boy1.id
		boy2.name == boy1.name
		boy2.age == data.age
		boy2.pets.size() == 2
		boy2.pets*.getClass().every { it == Pet }
		['Goldie', 'Dottie'].every { it in boy2.pets.name }
		boy2.pets.every { it.species == 'Goldfish' }
	}

	void 'can serialize an instance'() {
		given:
		def pet1 = new Pet(name: 'Goldie', species: 'Goldfish').save(failOnError: true)
		def pet2 = new Pet(name: 'Dottie', species: 'Goldfish').save(failOnError: true)
		def boy = new Boy(name: 'Alex', age: 2, pets: [pet1, pet2]).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(boy)
		json.pets.get(0).id.asLong == pet1.id
		json.pets.get(0).name.asString == pet1.name
		json.pets.get(0).species.asString == pet1.species
		json.pets.get(1).id.asLong == pet2.id
		json.pets.get(1).name.asString == pet2.name
		json.pets.get(1).species.asString == pet2.species
	}
}

@Entity
class Boy {
	String name
	int age
	static hasMany = [pets: Pet]
}

@Entity
class Pet {
	String name
	String species
}

