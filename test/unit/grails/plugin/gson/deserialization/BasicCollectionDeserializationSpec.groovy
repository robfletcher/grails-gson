package grails.plugin.gson.deserialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Pirate)
class BasicCollectionDeserializationSpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [
			name: 'Blackbeard',
			commands: ["Queen Anne's Revenge", 'Adventure']
		]
		def json = gson.toJson(data)

		when:
		def pirate = gson.fromJson(json, Pirate)

		then:
		pirate.name == data.name
		pirate.commands == data.commands
	}

	void 'can deserialize an existing instance'() {
		given:
		def pirate1 = new Pirate(name: 'Blackbeard', commands: ["Queen Anne's Revenge"]).save(failOnError: true)

		and:
		def data = [
			id: pirate1.id,
			commands: ["Queen Anne's Revenge", 'Adventure']
		]
		def json = gson.toJson(data)

		when:
		def pirate2 = gson.fromJson(json, Pirate)

		then:
		pirate2.id == pirate1.id
		pirate2.name == pirate1.name
		pirate2.commands == data.commands
	}
}

@Entity
class Pirate {
	String name
	List<String> commands
}
