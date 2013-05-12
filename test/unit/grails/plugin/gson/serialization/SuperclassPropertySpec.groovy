package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*

@Mock([SuperPerson, SuperEmployee])
class SuperclassPropertySpec extends Specification {

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
	
	def "can serialize instance of sub class"(){
		given:
		SuperEmployee employee = new SuperEmployee(name: 'Mark', employeeNumber: 'E1234' ).save(failOnError: true)
		
		when:
		def json = gson.toJsonTree(employee)
		
		then:
		json.name.asString == employee.name
		and:
		json.employeeNumber.asString == employee.employeeNumber
	}
	
	def "can deserialize into a new instance"(){
		given:
		def data = [
			name: 'Mark',
			employeeNumber: 'E1234'
		]
		def json = gson.toJson(data)
		
		when:
		def employee = gson.fromJson(json, SuperEmployee)
		
		then:
		employee.name == data.name
		employee.employeeNumber == data.employeeNumber
	}
	
	def "can deserialize into a existing instance"(){
		given:
		def employee1 = new SuperEmployee(name: 'Mark', employeeNumber: 'E1234' ).save(failOnError: true)
		and:
		def data = [
			id: employee1.id,
			name: 'Mark',
			employeeNumber: 'E1234'
		]
		
		def json = gson.toJson(data)
		
		when:
		def employee2 = gson.fromJson(json, SuperEmployee)
		
		then:
		employee2.id == employee1.id
		employee2.name == data.name
		employee2.employeeNumber == data.employeeNumber
	}
	
}

@Entity
class SuperPerson{
	String name
}

@Entity
class SuperEmployee extends SuperPerson{
	String employeeNumber
}