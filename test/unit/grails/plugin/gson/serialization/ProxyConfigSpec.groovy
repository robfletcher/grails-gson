package grails.plugin.gson.serialization

import com.google.gson.GsonBuilder
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.*

/**
 * Copyright Tom Dunstan 2013. All rights reserved.
 */
@Mock([BlogPost, Comment])
class ProxyConfigSpec extends Specification {

    def isInitialized

    void setup() {
        defineBeans {
            proxyHandler MyEntityProxyHandler
            domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
            domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
            gsonBuilder(GsonBuilderFactory) { bean ->
                bean.singleton = false
                pluginManager = ref('pluginManager')
            }
        }
    }

    void cleanup() {
        grailsApplication.with {
            config.clear()
            configChanged()
        }
    }

    @Unroll('output #description collection if proxy is #proxyState, resolveProxies is #resolveProxiesConfig and serializeProxies is #serializeProxiesConfig')
    void 'output contains collection depending on config and initialized state'() {
        given:
            grailsApplication.with {
                config.grails.converters.gson.resolveProxies = resolveProxiesConfig
                config.grails.converters.gson.serializeProxies = serializeProxiesConfig
                configChanged()
            }

        and:
            def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
            def gson = gsonBuilder.create()

        and:
            def blogPost = new BlogPost(content: 'hi there').addToComments(new Comment(content: 'you suck')).save(flush: true, failOnError: true)

        and: 'collection not initialized'
            def myEntityProxyHandler = applicationContext.getBean('proxyHandler', MyEntityProxyHandler)
            myEntityProxyHandler.answer = isInitialized

        expect:
            gson.toJson(blogPost) == expectedOutput

        where:
			serializeProxiesConfig | resolveProxiesConfig | isInitialized | expectOutputToContainCollection
			null                   | null                 | false         | true
			false                  | null                 | false         | false
			false                  | false                | false         | false
			true                   | false                | false         | false
			false                  | false                | true          | false
			true                   | false                | true          | true

			expectedOutput = expectOutputToContainCollection ? '{"id":1,"comments":[{"id":1,"content":"you suck"}],"content":"hi there"}' : '{"id":1,"content":"hi there"}'
			description = expectOutputToContainCollection ? "contains" : "does not contain"
			proxyState = isInitialized ? "initialized" : "not initialized"
	}
}

@Entity
class BlogPost {
    String content
    static hasMany = [comments: Comment]
}

@Entity
class Comment {
    String content
    static belongsTo = [blogPost: BlogPost]
}

class MyEntityProxyHandler extends DefaultEntityProxyHandler {
    boolean answer
    @Override
    public boolean isInitialized(Object o) {
        answer
    }
    @Override
    public boolean isProxy(Object o) {
        return o instanceof Collection
    }
}
