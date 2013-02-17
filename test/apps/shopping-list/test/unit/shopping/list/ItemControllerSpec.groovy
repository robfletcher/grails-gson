package shopping.list

import javax.servlet.http.HttpServletResponse
import grails.plugin.gson.*
import grails.test.mixin.*
import spock.lang.Specification
import static shopping.list.ItemController.SC_UNPROCESSABLE_ENTITY

@TestFor(ItemController)
@Mock(Item)
class ItemControllerSpec extends Specification {

	void setup() {
		def enhancer = new ArtefactEnhancer(grailsApplication)
		enhancer.enhanceControllers()
		enhancer.enhanceDomains()
		enhancer.enhanceRequest()

		def gsonFactory = new GsonFactory(grailsApplication)
		HttpServletResponse.metaClass.getContentAsJson = {->
			gsonFactory.createGson().fromJson(delegate.contentAsString, Map)
		}
	}

	void 'save returns a 422 given invalid JSON'() {
		given:
		def item = new Item(description: 'Gin', quantity: 1, unit: 'bottle').save(failOnError: true, flush: true)

		and:
		request.contentType = 'application/json'
		request.content = '{description: "", quantity: 0}'.bytes

		when:
		controller.update(item.id, item.version)

		then:
		response.status == SC_UNPROCESSABLE_ENTITY
		response.contentType == 'application/json;charset=UTF-8'
		response.contentAsJson.errors[0] == 'Property [description] of class [class shopping.list.Item] cannot be blank'
		response.contentAsJson.errors[1] == 'Property [quantity] of class [class shopping.list.Item] with value [0] is less than minimum value [1]'
	}
}