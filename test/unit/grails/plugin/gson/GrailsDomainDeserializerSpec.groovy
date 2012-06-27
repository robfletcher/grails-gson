package grails.plugin.gson

import grails.test.mixin.Mock
import spock.lang.Specification
import com.google.gson.*

@Mock([Person, Pet])
class GrailsDomainDeserializerSpec extends Specification {

	Gson gson

    void setup() {
        def builder = new GsonBuilder()
        builder.registerTypeAdapter Person, new GrailsDomainDeserializer(grailsApplication: grailsApplication)
        builder.registerTypeAdapter Pet, new GrailsDomainDeserializer(grailsApplication: grailsApplication)
        gson = builder.create()
    }

    void 'can create a new instance from json'() {
        given:
        def data = [
        	name: 'Alex',
        	age: 3,
            address: [
                number: 7,
                street: 'Gosberton Road'
            ],
            pets: [
            	[name: 'Goldie', species: 'Goldfish'],
            	[name: 'Dottie', species: 'Goldfish']
            ]
        ]
        def json = gson.toJson(data)

        when:
        def person = gson.fromJson(json, Person)

        then:
        person.name == data.name
        person.age == data.age
        person.address.number == data.address.number
        person.address.street == data.address.street
        person.pets.size() == 2
        person.pets*.getClass().every { it == Pet }
        ['Goldie', 'Dottie'].every { it in person.pets.name }
        person.pets.every { it.species == 'Goldfish' }
    }

    void 'can update an existing instance with json'() {
        given:
        def pet1 = new Pet(name: 'Goldy', species: 'Goldfish').save(failOnError: true)
        def person1 = new Person(name: 'Alex', age: 2, pets: [pet1]).save(failOnError: true)

        and:
        def data = [
            id: person1.id,
            age: 3,
            address: [
                number: 7,
                street: 'Gosberton Road'
            ],
            pets: [
            	[id: pet1.id, name: 'Goldie'],
            	[name: 'Dottie', species: 'Goldfish']
            ]
        ]
        def json = gson.toJson(data)

        when:
        def person2 = gson.fromJson(json, Person)

        then:
        person2.id == person1.id
        person2.name == person1.name
        person2.age == data.age
        person2.address.number == data.address.number
        person2.address.street == data.address.street
        person2.pets.size() == 2
		person2.pets*.getClass().every { it == Pet }
		['Goldie', 'Dottie'].every { it in person2.pets.name }
        person2.pets.every { it.species == 'Goldfish' }
    }
}