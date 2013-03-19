package grails.plugin.gson.test

import javax.servlet.http.HttpServletResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import grails.plugin.gson.adapters.*
import grails.plugin.gson.metaclass.ArtefactEnhancer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.support.GrailsUnitTestMixin
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
		def enhancer = new ArtefactEnhancer(
				grailsApplication,
				applicationContext.getBean('gsonBuilder', GsonBuilder),
				applicationContext.getBean('domainDeserializer', GrailsDomainDeserializer)
		)
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()

		def parser = new JsonParser()
		HttpServletResponse.metaClass.getGSON = {->
			parser.parse delegate.contentAsString
		}
	}

}
