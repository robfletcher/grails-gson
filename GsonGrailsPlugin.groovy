import grails.plugin.gson.ArtefactEnhancer

class GsonGrailsPlugin {
    
    def version = '1.0'
    def grailsVersion = '2.0 > *'
    def dependsOn = [:]
    def pluginExcludes = [
        'grails-app/views/**/*'
    ]

    def title = 'Gson Plugin'
    def author = 'Rob Fletcher'
    def authorEmail = 'rob@freeside.co'
    def description = 'Provides alternate JSON (de)serialization using Google\'s Gson library'
    def documentation = 'http://grails.org/plugin/gson'
    def license = 'APACHE'
    def organization = [name: 'Freeside Software', url: 'http://freeside.co']
    def issueManagement = [system: 'GitHub', url: 'https://github.com/robfletcher/grails-gson/issues']
    def scm = [url: 'https://github.com/robfletcher/grails-gson']

    def doWithDynamicMethods = { ctx ->
        def enhancer = new ArtefactEnhancer(application)
		enhancer.enhanceRequest()
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
    }

}
