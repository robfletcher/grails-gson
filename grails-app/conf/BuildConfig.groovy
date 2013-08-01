grails.project.work.dir = 'target'
grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
grails.project.target.level = 1.6

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        compile 'com.google.code.gson:gson:2.2.4'
        test('joda-time:joda-time:2.2') {
            export = false
        }
		test('org.spockframework:spock-grails-support:0.7-groovy-2.0') {
			export = false
		}
    }

    plugins {
        build(':release:2.2.1', ':rest-client-builder:1.0.3') {
            export = false
        }
        test(':spock:0.7') {
            export = false
			exclude 'spock-grails-support'
        }
    }
}

grails.project.repos.grailsCentral.username = System.getenv("GRAILS_CENTRAL_USERNAME")
grails.project.repos.grailsCentral.password = System.getenv("GRAILS_CENTRAL_PASSWORD")
