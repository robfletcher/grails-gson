package grails.plugin.gson.binding

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/2')
@Mock(User)
class BlacklistedPropertySpec extends Specification {

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

	void 'blacklisted properties are not deserialized'() {
		given:
		def data = [
		        username: 'rob',
				password: 'secret'
		]
		def json = gson.toJson(data)

		when:
		def user = gson.fromJson(json, User)

		then:
		user.username == data.username
		user.password == null
	}

}

@Entity
class User {
	String username
	String password

	static constraints = {
		password bindable: false
	}
}