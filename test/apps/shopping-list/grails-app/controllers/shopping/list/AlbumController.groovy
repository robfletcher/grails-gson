package shopping.list

import grails.plugin.gson.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*

class AlbumController {

	public static final String X_PAGINATION_TOTAL = 'X-Pagination-Total'
	public static final int SC_UNPROCESSABLE_ENTITY = 422

	def beforeInterceptor = [action: this.&checkRequestIsJson, only: ['save', 'update']]

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		response.addIntHeader X_PAGINATION_TOTAL, Album.count()
		render Album.list(params) as GSON
	}

	def save() {
		def albumInstance = new Album(request.GSON)
		log.error albumInstance
		if (albumInstance.save(flush: true)) {
			respondCreated albumInstance
		} else {
			respondUnprocessableEntity albumInstance
		}
	}

	def show(Long id) {
		def albumInstance = Album.get(id)
		if (albumInstance) {
			respondFound albumInstance
		} else {
			respondNotFound id
		}
	}

	def update(Long id, Long version) {
		def albumInstance = Album.get(id)
		if (!albumInstance) {
			respondNotFound id
			return
		}

		if (version != null) {
			if (albumInstance.version > version) {
				respondConflict(albumInstance)
				return
			}
		}

		albumInstance.properties = request.GSON
		log.error albumInstance

		if (albumInstance.save(flush: true)) {
			respondUpdated albumInstance
		} else {
			respondUnprocessableEntity albumInstance
		}
	}

	def delete(Long id) {
		def albumInstance = Album.get(id)
		if (!albumInstance) {
			respondNotFound id
			return
		}

		try {
			albumInstance.delete(flush: true)
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

	private void respondFound(Album albumInstance) {
		response.status = SC_OK
		render albumInstance as GSON
	}

	private void respondCreated(Album albumInstance) {
		response.status = SC_CREATED
		render albumInstance as GSON
	}

	private void respondUpdated(Album albumInstance) {
		response.status = SC_OK
		render albumInstance as GSON
	}

	private void respondUnprocessableEntity(Album albumInstance) {
		def responseBody = [:]
		responseBody.errors = albumInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_UNPROCESSABLE_ENTITY
		render responseBody as GSON
	}

	private void respondNotFound(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.found.message', args: [message(code: 'item.label', default: 'Album'), id])
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	private void respondConflict(Album albumInstance) {
		albumInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
				[message(code: 'item.label', default: 'Album')] as Object[],
				'Another user has updated this Album while you were editing')
		def responseBody = [:]
		responseBody.errors = albumInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_CONFLICT
		render responseBody as GSON
	}

	private void respondDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.deleted.message', args: [message(code: 'item.label', default: 'Album'), id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondNotDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: 'item.label', default: 'Album'), id])
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
