package shopping.list

import javax.servlet.http.HttpServletResponse
import com.google.gson.JsonParser
import grails.plugin.gson.ArtefactEnhancer
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

		HttpServletResponse.metaClass.getContentAsJson = {->
			new JsonParser().parse(delegate.contentAsString)
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
		response.contentAsJson.errors.get(0).asString == 'Property [description] of class [class shopping.list.Item] cannot be blank'
		response.contentAsJson.errors.get(1).asString == 'Property [quantity] of class [class shopping.list.Item] with value [0] is less than minimum value [1]'
	}
}