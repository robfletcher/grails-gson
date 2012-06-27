package grails.plugin.gson.deserialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(RockStar)
class BasicDeserializationSpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [firstName: 'Ziggy', lastName: 'Stardust']
		def json = gson.toJson(data)

		when:
		def p = gson.fromJson(json, RockStar)

		then:
		p.firstName == data.firstName
		p.lastName == data.lastName
	}

	void 'can deserialize an existing instance'() {
		given:
		def p1 = new RockStar(firstName: 'David', lastName: 'Jones').save(failOnError: true)

		and:
		def data = [id: p1.id, lastName: 'Bowie']
		def json = gson.toJson(data)

		when:
		def p2 = gson.fromJson(json, RockStar)

		then:
		p2.firstName == p1.firstName
		p2.lastName == data.lastName
	}

}

@Entity
class RockStar {
	String firstName
	String lastName
}
