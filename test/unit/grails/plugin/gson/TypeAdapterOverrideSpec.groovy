package grails.plugin.gson

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Person)
class TypeAdapterOverrideSpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			personTypeAdapterFactory(PersonTypeAdapterFactory)
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

	void 'type adapter registered in spring context will override default serialization'() {
		given:
		def person = new Person(name: 'Rob')

		expect:
		gson.toJson(person) == '"Rob"'
	}

	void 'type adapter registered in spring context will override default deserialization'() {
		given:
		def person = gson.fromJson('"Rob"', Person)

		expect:
		person.name == 'Rob'
	}

}

@Entity
class Person {
	String name
}

class PersonTypeAdapterFactory implements TypeAdapterFactory {
	@Override
	TypeAdapter create(Gson gson, TypeToken type) {
		type.rawType == Person ? new PersonTypeAdapter() : null
	}
}

class PersonTypeAdapter extends TypeAdapter<Person> {
	@Override
	Person read(JsonReader reader) {
		new Person(name: reader.nextString())
	}

	@Override
	void write(JsonWriter writer, Person value) {
		writer.value value.name
	}
}