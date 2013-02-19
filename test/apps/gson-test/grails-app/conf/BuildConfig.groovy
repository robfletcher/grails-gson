grails.servlet.version = '3.0'
grails.project.work.dir = 'target'
grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.dependency.resolution = {

	inherits 'global'
	log 'error'
	checksums true
	legacyResolve false

	repositories {
		inherits true

		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'com.google.code.gson:gson:2.2.2'

		test 'org.spockframework:spock-grails-support:0.7-groovy-2.0'
		test 'org.codehaus.groovy.modules.http-builder:http-builder:0.6'
	}

	plugins {
		runtime ":hibernate:$grailsVersion"

		build ":tomcat:$grailsVersion"

		test ':fixtures:1.2'
		test(':spock:0.7') {
			exclude 'spock-grails-support'
		}
	}

}

grails.plugin.location.gson = '../../..'
