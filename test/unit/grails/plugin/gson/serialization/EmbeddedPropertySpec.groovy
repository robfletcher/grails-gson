package grails.plugin.gson.serialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.Specification
import com.google.gson.JsonObject

@Mock(Person)
class EmbeddedPropertySpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication, applicationContext.pluginManager).createGson()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [
			name: 'Rob',
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
		person.address.number == data.address.number
		person.address.street == data.address.street
	}

	void 'can deserialize an existing instance'() {
		given:
		def person1 = new Person(name: 'Rob', address: new Address(number: 62, street: 'Goldsboro Road')).save(failOnError: true)

		and:
		def data = [
			id: person1.id,
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
		person2.address.number == data.address.number
		person2.address.street == data.address.street
	}

	void 'can serialize an instance'() {
		given:
		def person = new Person(name: 'Rob', address: new Address(number: 7, street: 'Gosberton Road')).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(person)
		json.address.number.asInt == person.address.number
		json.address.street.asString == person.address.street
	}
}

@Entity
class Person {
	String name
	Address address
	static embedded = ['address']
}

class Address {
	int number
	String street
}