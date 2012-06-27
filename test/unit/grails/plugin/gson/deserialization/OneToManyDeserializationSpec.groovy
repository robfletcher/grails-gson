package grails.plugin.gson.deserialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Boy, Pet])
class OneToManyDeserializationSpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()
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

