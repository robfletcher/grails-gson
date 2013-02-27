package grails.plugin.gson.deserialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/24')
@Mock([Album, Artist, Biography, Cover])
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

	void 'bi-directional associations are populated in both directions when deserializing one-to-one'() {
		given:
		def data = [
				title: 'Station to Station',
				cover: [image: new byte[0]]
		]
		def json = gson.toJson(data)

		when:
		def album = gson.fromJson(json, Album)

		then:
		album.cover.album == album
	}

	void 'bi-directional associations are populated in both directions when deserializing one-to-one using hasOne'() {
		given:
		def data = [
		        name: 'David Bowie',
				bio: [text: 'David Robert Jones (born 8 January 1947), known by his' +
						' stage name David Bowie (pron.: /ˈboʊ.i/ boh-ee),[1]' +
						' is an English musician, actor, record producer and arranger...']
		]
		def json = gson.toJson(data)

		when:
		def artist = gson.fromJson(json, Artist)

		then:
		artist.bio.artist == artist
	}

}

@Entity
class Album {
	String title
	Cover cover
	static belongsTo = [artist: Artist]
}

@Entity
class Artist {
	String name
	static hasMany = [albums: Album]
	static hasOne = [bio: Biography]
}

@Entity
class Biography {
	Artist artist
	String text
}

@Entity
class Cover {
	byte[] image
	static belongsTo = [album: Album]
}