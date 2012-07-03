package grails.plugin.gson.serialization

import com.google.gson.Gson
import grails.persistence.Entity
import grails.plugin.gson.GsonFactory
import grails.test.mixin.Mock
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonReader
import org.joda.time.format.DateTimeFormatter

@Mock(Reminder)
class NonStandardPropertyTypeSpec extends Specification {

    Gson gson
    DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecond()

    void setup() {
        def factory = new GsonFactory(grailsApplication)
        factory.registerTypeAdapter(LocalDateTime, new TypeAdapter<LocalDateTime>() {
            @Override
            void write(JsonWriter jsonWriter, LocalDateTime t) {
                jsonWriter.value(t.toString(formatter))
            }

            @Override
            LocalDateTime read(JsonReader jsonReader) {
                formatter.parseLocalDateTime(jsonReader.nextString())
            }
        })
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