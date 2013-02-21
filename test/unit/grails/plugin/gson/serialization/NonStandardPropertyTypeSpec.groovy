package grails.plugin.gson.serialization

import com.google.gson.*
import com.google.gson.stream.*
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import groovy.transform.TupleConstructor
import org.joda.time.LocalDateTime
import org.joda.time.format.*
import spock.lang.Specification

@Mock(Reminder)
class NonStandardPropertyTypeSpec extends Specification {

	Gson gson
	def formatter = ISODateTimeFormat.dateHourMinuteSecond()

	void setup() {
		def factory = new GsonFactory(applicationContext, grailsApplication, applicationContext.pluginManager)
		factory.registerTypeAdapter(LocalDateTime, new LocalDateTimeAdapter(formatter))
		gson = factory.createGson()
	}

	void 'can deserialize a non-standard property type'() {
		given:
		def data = [
				label: 'Cocktails at the bar',
				time: '2012-06-28T17:00:00'
		]
		def json = gson.toJson(data)

		when:
		def reminder = gson.fromJson(json, Reminder)

		then:
		reminder.label == data.label
		reminder.time == formatter.parseLocalDateTime(data.time)
	}

	void 'can serialize an instance with a non-standard property type'() {
		given:
		def reminder = new Reminder(label: 'Cocktails at the bar', time: new LocalDateTime(2012, 6, 28, 17, 0)).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(reminder)
		json.time.asString == '2012-06-28T17:00:00'
	}

}

@Entity
class Reminder {
	String label
	LocalDateTime time
}

@TupleConstructor
class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

	final DateTimeFormatter formatter

	@Override
	void write(JsonWriter jsonWriter, LocalDateTime t) {
		jsonWriter.value(t.toString(formatter))
	}

	@Override
	LocalDateTime read(JsonReader jsonReader) {
		formatter.parseLocalDateTime(jsonReader.nextString())
	}
}