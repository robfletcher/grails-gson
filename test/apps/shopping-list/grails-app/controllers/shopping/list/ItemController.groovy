package shopping.list

import grails.plugin.gson.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*

class ItemController {

	public static final String X_PAGINATION_TOTAL = 'X-Pagination-Total'
	public static final int SC_UNPROCESSABLE_ENTITY = 422

	def beforeInterceptor = [action: this.&checkRequestIsJson, only: ['save', 'update']]

	def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
		response.addIntHeader X_PAGINATION_TOTAL, Item.count()
        render Item.list(params) as GSON
    }

    def save() {
        def itemInstance = new Item(request.GSON)
		if (itemInstance.save(flush: true)) {
			respondCreated itemInstance
		} else {
			respondUnprocessableEntity itemInstance
		}
    }

	def show(Long id) {
        def itemInstance = Item.get(id)
		if (itemInstance) {
			render itemInstance as GSON
		} else {
			respondNotFound id
		}
    }

	def update(Long id, Long version) {
		def itemInstance = Item.get(id)
        if (!itemInstance) {
			respondNotFound id
			return
        }

        if (version != null) {
            if (itemInstance.version > version) {
				respondConflict(itemInstance)
                return
            }
        }

        itemInstance.properties = params

		if (itemInstance.save(flush: true)) {
			respondUpdated itemInstance
		} else {
			respondUnprocessableEntity itemInstance
		}
    }

	def delete(Long id) {
        def itemInstance = Item.get(id)
        if (!itemInstance) {
			respondNotFound id
            return
        }

        try {
            itemInstance.delete(flush: true)
			respondDeleted id
		} catch (DataIntegrityViolationException e) {
			respondNotDeleted id
		}
    }

	private checkRequestIsJson() {
		if (request.contentType != 'application/json') {
			respondNotAcceptable()
			return false
		}
	}

	private void respondCreated(Item itemInstance) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.created.message', args: [message(code: 'item.label', default: 'Item'), itemInstance.id])
		response.status = SC_CREATED
		render responseBody as GSON
	}

	private void respondUpdated(Item itemInstance) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.updated.message', args: [message(code: 'item.label', default: 'Item'), itemInstance.id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondUnprocessableEntity(Item itemInstance) {
		def responseBody = [:]
		responseBody.errors = itemInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_UNPROCESSABLE_ENTITY
		render responseBody as GSON
	}

	private void respondNotFound(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.found.message', args: [message(code: 'item.label', default: 'Item'), id])
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	private void respondConflict(Item itemInstance) {
		itemInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
				[message(code: 'item.label', default: 'Item')] as Object[],
				'Another user has updated this Item while you were editing')
		def responseBody = [:]
		responseBody.errors = itemInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_CONFLICT
		render responseBody as GSON
	}

	private void respondDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.deleted.message', args: [message(code: 'item.label', default: 'Item'), id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondNotDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: 'item.label', default: 'Item'), id])
		response.status = SC_INTERNAL_SERVER_ERROR
		render responseBody as GSON
	}

	private void respondNotAcceptable() {
		response.status = SC_NOT_ACCEPTABLE
		response.contentLength = 0
		response.outputStream.flush()
		response.outputStream.close()
	}

}
