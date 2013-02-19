package grails.plugin.gson.serialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/13')
@Mock([Album, Artist])
class BidirectionalPropertySpec extends Specification {

	Gson gson
	Artist artist
	Album album1, album2, album3

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()

		artist = new Artist(name: 'David Bowie').save(failOnError: true)
		album1 = new Album(title: 'Hunky Dory', artist: artist).save(failOnError: true)
		album2 = new Album(title: 'Aladdin Sane', artist: artist).save(failOnError: true)
		album3 = new Album(title: 'Low', artist: artist).save(failOnError: true)
	}

	void 'can serialize a full graph from the owning side of a relationship'() {
		expect:
		def json = gson.toJsonTree(artist)
		json.name.asString == artist.name
		json.albums.size() == 3
		json.albums.collect { it.title.asString }.sort() == [album1, album2, album3].title.sort()
	}

	void 'serialization stops at a non-owning relationship'() {
		expect:
		def json = gson.toJsonTree(album1)
		json.title.asString == album1.title
		json.artist == null
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
