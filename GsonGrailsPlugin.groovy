import grails.plugin.gson.adapters.GrailsDomainDeserializer
import grails.plugin.gson.adapters.GrailsDomainSerializer
import grails.plugin.gson.converters.GsonParsingParameterCreationListener
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler

class GsonGrailsPlugin {
    
    def version = '1.1.4'
    def grailsVersion = '2.0 > *'
    def dependsOn = [:]
	def loadAfter = ['controllers', 'converters']
    def pluginExcludes = [
        'grails-app/views/**/*'
    ]

    def title = 'Gson Plugin'
    def author = 'Rob Fletcher'
    def authorEmail = 'rob@freeside.co'
    def description = 'Provides alternate JSON (de)serialization using Google\'s Gson library'
    def documentation = 'http://git.io/grails-gson'
    def license = 'APACHE'
    def organization = [name: 'Freeside Software', url: 'http://freeside.co']
    def issueManagement = [system: 'GitHub', url: 'https://github.com/robfletcher/grails-gson/issues']
    def scm = [url: 'https://github.com/robfletcher/grails-gson']

	def doWithSpring = {
		if (!manager?.hasGrailsPlugin('hibernate')) {
			proxyHandler DefaultEntityProxyHandler
		}

		domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
		domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
		gsonBuilder GsonBuilderFactory
		jsonParsingParameterCreationListener GsonParsingParameterCreationListener, ref('gsonBuilder')
	}

    def doWithDynamicMethods = { ctx ->
        def enhancer = new grails.plugin.gson.api.ArtefactEnhancer(application, ctx.gsonBuilder, ctx.domainDeserializer)
		enhancer.enhanceRequest()
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
    }

}
