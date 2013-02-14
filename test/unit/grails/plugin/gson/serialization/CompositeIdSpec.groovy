package grails.plugin.gson.serialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.*

@Mock(Coordinate)
class CompositeIdSpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/7')
	void 'can deserialize an existing instance with a composite id'() {
		given:
		def coord1 = new Coordinate(x: '3.38', y: '-54.43', feature: 'Bouvet Island').save(failOnError: true)

		and:
		def data = [x: '3.38', y: '-54.43']
		def json = gson.toJson(data)

		when:
		def coord2 = gson.fromJson(json, Coordinate)

		then:
		coord2.x == coord1.x
		coord2.y == coord1.y
		coord2.feature == coord1.feature
	}

	void 'can serialize an instance with a composite id'() {
		given:
		def coord = new Coordinate(x: '3.38', y: '-54.43', feature: 'Bouvet Island').save(failOnError: true)

		expect:
		def json = gson.toJsonTree(coord)
		json.x.asString == coord.x
		json.y.asString == coord.y
	}

}

@Entity
class Coordinate {
	String x
	String y
	String feature
	static mapping = {
		id composite: ['x', 'y']
	}
}