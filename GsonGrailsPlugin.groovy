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
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

}
