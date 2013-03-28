package grails.plugin.gson.test

import com.google.gson.JsonParser
import grails.converters.JSON
import grails.plugin.spock.IntegrationSpec
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Issue
import spock.lang.Unroll

@Issue("https://github.com/robfletcher/grails-gson/issues/30")
@Unroll
class BindingSpec extends IntegrationSpec {

	def artist = new Artist(name: "David Jones")

	void setup() {
		artist.save(flush: true, failOnError: true)
	}

	void "can bind domain instance properties from a GrailsParameterMap"() {
		given:
		def request = RequestContextHolder.currentRequestAttributes().request
		request.setParameter("name", "David Bowie")

		and:
		def parameterMap = new GrailsParameterMap(request)

		when:
		artist.properties = parameterMap

		then:
		artist.name == parameterMap.name
	}

	void "can bind domain instance properties from a #type"() {
		when:
		artist.properties = parameter

		then:
		artist.name == value

		where:
		parameter << [
				new JsonParser().parse('{"name":"David Bowie"}'),
				JSON.parse('{"name":"David Bowie"}'),
				[name: "David Bowie"]
		]
		value = "David Bowie"
		type = parameter.getClass().name
	}

	void "can bind a new domain instance from a GrailsParameterMap"() {
		given:
		def request = RequestContextHolder.currentRequestAttributes().request
		request.setParameter("name", "David Bowie")

		and:
		def parameterMap = new GrailsParameterMap(request)

		when:
		artist = new Artist(parameterMap)

		then:
		artist.name == parameterMap.name
	}

	void "can bind a new domain instance from a #type"() {
		when:
		artist = new Artist(parameter)

		then:
		artist.name == value

		where:
		parameter << [
				new JsonParser().parse('{"name":"David Bowie"}'),
				JSON.parse('{"name":"David Bowie"}'),
				[name: "David Bowie"]
		]
		value = "David Bowie"
		type = parameter.getClass().name
	}

}
