package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Geek, Site])
class HasManyMapSpec extends Specification {

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
			name: 'Rob',
			sites: [
				homepage: [url: 'http://freeside.co'],
				blog: [url: 'http://hipsterdevstack.tumblr.com']
			]
		]
		def json = gson.toJson(data)

		when:
		def geek = gson.fromJson(json, Geek)

		then:
		geek.name == data.name
		geek.sites.size() == 2
		geek.sites.keySet() == data.sites.keySet()
		geek.sites.values()*.getClass().every { it == Site }
		geek.sites.homepage.url.toString() == data.sites.homepage.url
		geek.sites.blog.url.toString() == data.sites.blog.url
	}

	void 'can deserialize an existing instance'() {
		given:
		def site1 = new Site(url: 'http://freeside.co'.toURL()).save(failOnError: true)
		def geek1 = new Geek(name: 'Rob', sites: [homepage: site1]).save(failOnError: true)

		and:
		def data = [
			id: geek1.id,
			sites: [
				homepage: [id: site1.id],
				blog: [url: 'http://hipsterdevstack.tumblr.com'],
				twitter: [url: 'http://twitter.com/rfletcherEW']
			]
		]
		def json = gson.toJson(data)

		when:
		def geek2 = gson.fromJson(json, Geek)

		then:
		geek2.id == geek1.id
		geek2.name == geek1.name
		geek2.sites.size() == 3
		geek2.sites.keySet() == ['homepage', 'blog', 'twitter'] as Set
		geek2.sites.values()*.getClass().every { it == Site }
		geek2.sites.homepage.url == geek1.sites.homepage.url
		geek2.sites.blog.url.toString() == data.sites.blog.url
		geek2.sites.twitter.url.toString() == data.sites.twitter.url
	}

	void 'can serialize an instance'() {
		given:
		def site1 = new Site(url: 'http://freeside.co'.toURL()).save(failOnError: true)
		def site2 = new Site(url: 'http://hipsterdevstack.tumblr.com'.toURL()).save(failOnError: true)
		def geek = new Geek(name: 'Rob', sites: [homepage: site1, tumblr: site2]).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(geek)
		json.sites.homepage.url.asString == site1.url.toString()
		json.sites.tumblr.url.asString == site2.url.toString()
	}
}

@Entity
class Geek {
	String name
	Map<String, Site> sites
	static hasMany = [sites: Site]
}

@Entity
class Site {
	URL url

	@Override
	String toString() {
		url
	}
}