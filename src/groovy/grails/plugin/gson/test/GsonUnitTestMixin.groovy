package grails.plugin.gson.test

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import grails.plugin.gson.adapters.*
import grails.plugin.gson.metaclass.ArtefactEnhancer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.*

class GsonUnitTestMixin extends GrailsUnitTestMixin {

	@BeforeClass
	static void initializeGsonDependencies() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	@Before
	void enhanceApplication() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		def domainDeserializer = applicationContext.getBean('domainDeserializer', GrailsDomainDeserializer)

		def enhancer = new ArtefactEnhancer(grailsApplication, gsonBuilder, domainDeserializer)
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()

		def parser = new JsonParser()

		HttpServletRequest.metaClass.setGSON = { JsonElement json ->
			delegate.contentType = 'application/json'
			delegate.content = new Gson().toJson(json).getBytes('UTF-8')
		}

		HttpServletRequest.metaClass.setGSON = { CharSequence json ->
			delegate.setGSON parser.parse(json.toString())
		}

		HttpServletResponse.metaClass.getGSON = {->
			parser.parse delegate.contentAsString
		}
	}

}
