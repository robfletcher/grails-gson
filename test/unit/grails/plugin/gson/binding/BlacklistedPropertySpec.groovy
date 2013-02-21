package grails.plugin.gson.binding

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import spock.lang.*

@Issue('https://github.com/robfletcher/grails-gson/issues/2')
@Mock(User)
class BlacklistedPropertySpec extends Specification {

	Gson gson

	void setup() {
		gson = new GsonFactory(applicationContext, grailsApplication, applicationContext.pluginManager).createGson()
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