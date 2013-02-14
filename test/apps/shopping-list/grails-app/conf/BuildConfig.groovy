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
    }

    plugins {
        runtime ":hibernate:$grailsVersion"

        build ":tomcat:$grailsVersion"
    }

}

grails.plugin.location.gson = '../../..'
