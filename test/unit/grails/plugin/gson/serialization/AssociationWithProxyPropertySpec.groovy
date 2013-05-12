package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*

@Mock([State, District, City])
class AssociationWithProxyPropertySpec extends Specification{
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
	
	def "can serialize instance"(){
		given:
		State state = new State(stateName: 'MyState').save(failOnError: true)
		District district = new District(districtName: 'MyDistrict', state: state).save(failOnError: true)
		City city = new City(cityName: 'MyCity', district: district).save(failOnError: true)
		
		when:
		def json = gson.toJsonTree(city)
		
		then:
		json.cityName.asString == city.cityName
		json.district.districtName.asString == district.districtName
		json.district.state.stateName.asString == state.stateName
	}
}

@Entity
class State{
	String stateName
}

@Entity
class District{
	State state
	String districtName
}

@Entity
class City{
	District district
	String cityName
}
