package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Book, Author])
class ManyToOnePropertySpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
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

	void 'can serialize an instance'() {
		given:
		def author = new Author(name: 'William Gibson').save(failOnError: true)
		def book = new Book(title: 'Virtual Light', author: author).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(book)
		json.author.id.asLong == author.id
		json.author.name.asString == author.name
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
