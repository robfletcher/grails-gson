package grails.plugin.gson

import grails.test.mixin.Mock
import spock.lang.Specification
import com.google.gson.*

@Mock(Person)
class GrailsDomainDeserializerSpec extends Specification {

	Gson gson

    void setup() {
        def builder = new GsonBuilder()
        builder.registerTypeAdapter Person, new GrailsDomainDeserializer()
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
    }

    void 'can update and existing instance with json'() {
        given:
        def person1 = new Person(name: 'Alex', age: 2).save(failOnError: true)

        and:
        def data = [
            id: person1.id,
            age: 3,
            address: [
                number: 7,
                street: 'Gosberton Road'
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
    }
}