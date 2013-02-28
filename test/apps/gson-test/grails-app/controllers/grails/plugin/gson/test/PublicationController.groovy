package grails.plugin.gson.test

import grails.plugin.gson.converters.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*
import static grails.plugin.gson.http.HttpConstants.*

class PublicationController {

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		response.addIntHeader X_PAGINATION_TOTAL, Publication.count()
		render Publication.list(params) as GSON
	}

	def save() {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def publicationInstance = new Publication(request.GSON)
		if (publicationInstance.save(flush: true)) {
			respondCreated publicationInstance
		} else {
			respondUnprocessableEntity publicationInstance
		}
	}

	def show() {
		def publicationInstance = Publication.get(params.id)
		if (publicationInstance) {
			respondFound publicationInstance
		} else {
			respondNotFound params.id
		}
	}

	def update() {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def publicationInstance = Publication.get(params.id)
		if (!publicationInstance) {
			respondNotFound params.id
			return
		}

		if (params.version != null) {
			if (publicationInstance.version > params.long('version')) {
				respondConflict(publicationInstance)
				return
			}
		}

		publicationInstance.properties = request.GSON

		if (publicationInstance.save(flush: true)) {
			respondUpdated publicationInstance
		} else {
			respondUnprocessableEntity publicationInstance
		}
	}

	def delete() {
		def publicationInstance = Publication.get(params.id)
		if (!publicationInstance) {
			respondNotFound params.id
			return
		}

		try {
			publicationInstance.delete(flush: true)
			respondDeleted params.id
		} catch (DataIntegrityViolationException e) {
			respondNotDeleted params.id
		}
	}

	private boolean requestIsJson() {
		GSON.isJson(request)
	}

	private void respondFound(Publication publicationInstance) {
		response.status = SC_OK
		render publicationInstance as GSON
	}

	private void respondCreated(Publication publicationInstance) {
		response.status = SC_CREATED
		render publicationInstance as GSON
	}

	private void respondUpdated(Publication publicationInstance) {
		response.status = SC_OK
		render publicationInstance as GSON
	}

	private void respondUnprocessableEntity(Publication publicationInstance) {
		def responseBody = [:]
		responseBody.errors = publicationInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_UNPROCESSABLE_ENTITY
		render responseBody as GSON
	}

	private void respondNotFound(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.found.message', args: [message(code: 'publication.label', default: 'Publication'), id])
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	private void respondConflict(Publication publicationInstance) {
		publicationInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
				[message(code: 'publication.label', default: 'Publication')] as Object[],
				'Another user has updated this Publication while you were editing')
		def responseBody = [:]
		responseBody.errors = publicationInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_CONFLICT
		render responseBody as GSON
	}

	private void respondDeleted(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.deleted.message', args: [message(code: 'publication.label', default: 'Publication'), id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondNotDeleted(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: 'publication.label', default: 'Publication'), id])
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
