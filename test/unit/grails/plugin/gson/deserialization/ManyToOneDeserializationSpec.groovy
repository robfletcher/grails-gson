package grails.plugin.gson.deserialization

import com.google.gson.Gson
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.Specification
import grails.persistence.Entity

@Mock([Book, Author])
class ManyToOneDeserializationSpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(grailsApplication).createGson()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [
			title: 'Zero History',
			author: [name: 'William Gibson']
		]
		def json = gson.toJson(data)

		when:
		def book = gson.fromJson(json, Book)

		then:
		book.title == data.title
		book.author.name == data.author.name
	}

	void 'can deserialize an existing instance'() {
		given:
		def author1 = new Author(name: 'Iain Banks').save(failOnError: true)
		def book1 = new Book(title: 'Excession', author: author1).save(failOnError: true)

		and:
		def data = [
			id: book1.id,
			author: [id: author1.id, name: 'Iain M. Banks']
		]
		def json = gson.toJson(data)

		when:
		def book2 = gson.fromJson(json, Book)

		then:
		book2.id == book1.id
		book2.title == book1.title
		book2.author.id == author1.id
		book2.author.name == data.author.name
	}
}

@Entity
class Book {
	String title
	Author author
}

@Entity
class Author {
	String name
}
