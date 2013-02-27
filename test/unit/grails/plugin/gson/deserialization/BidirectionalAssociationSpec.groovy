package grails.plugin.gson.deserialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/24')
@Mock([Album, Artist])
class BidirectionalAssociationSpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	void setup() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		gson = gsonBuilder.create()
	}

	void 'bi-directional associations are populated in both directions when deserializing one-to-many'() {
		given:
		def data = [
				name: 'David Bowie',
				albums: [
						[title: 'Low'],
						[title: '"Heroes"'],
						[title: 'Lodger']
				]
		]
		def json = gson.toJson(data)

		when:
		def artist = gson.fromJson(json, Artist)

		then:
		artist.albums.artist.every { it == artist }
	}

	void 'bi-directional associations are populated in both directions when deserializing many-to-one'() {
		given:
		def data = [
				title: 'Station to Station',
				artist: [name: 'David Bowie']
		]
		def json = gson.toJson(data)

		when:
		def album = gson.fromJson(json, Album)

		then:
		album.artist.albums == [album]
	}

}

@Entity
class Album {
	String title
	static belongsTo = [artist: Artist]
}

@Entity
class Artist {
	String name
	static hasMany = [albums: Album]
}
