package grails.plugin.gson.deserialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/24')
@Mock([Album, Artist, Biography, Cover, Genre, Musician])
class BidirectionalAssociationSpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			domainSerializer GrailsDomainSerializer, ref('grailsApplication')
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
		album.artist.albums == [album] as Set
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

	void 'bi-directional associations are populated in both directions when deserializing many-to-many'() {
		given:
		def data = [
				title: 'Young Americans',
				genres: [
						[name: 'Funk'],
						[name: 'Rock'],
						[name: 'Plastic Soul']
				]
		]
		def json = gson.toJson(data)

		when:
		def album = gson.fromJson(json, Album)

		then:
		album.genres.every {
			it.albums == [album] as Set
		}
	}

	void 'bi-directional associations are populated in both directions when deserializing one-to-many Maps'() {
		given:
		def data = [
				title: 'The Rise and Fall of Ziggy Stardust and the Spiders from Mars',
				personnel: [
						vocals: [name: 'David Bowie'],
						guitar: [name: 'Mick Ronson'],
						bass: [name: 'Trevor Bolder'],
						drums: [name: 'Mick Woodmansey']
				]
		]
		def json = gson.toJson(data)

		when:
		def album = gson.fromJson(json, Album)

		then:
		album.personnel.values().album.every { it == album }
	}

}

@Entity
class Album {
	String title
	Map<String, Musician> personnel
	Cover cover                         // one-to-one
	static belongsTo = [artist: Artist] // many-to-one
	static hasMany = [
			personnel: Musician,        // one-to-many via Map
			genres: Genre               // many-to-many
	]
}

@Entity
class Artist {
	String name
	static hasMany = [albums: Album]    // one-to-many
	static hasOne = [bio: Biography]    // one-to-one via hasOne
}

@Entity
class Biography {
	Artist artist                       // one-to-one via hasOne
	String text
}

@Entity
class Cover {
	byte[] image
	static belongsTo = [album: Album]   // one-to-one
}

@Entity
class Genre {
	String name
	static hasMany = [albums: Album]    // many-to-many
	static belongsTo = Album
}

@Entity
class Musician {
	String name
	static belongsTo = [album: Album]   // many-to-one via Map
}