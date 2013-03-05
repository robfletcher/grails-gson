package grails.plugin.gson.test

import grails.plugin.gson.converters.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*
import static grails.plugin.gson.http.HttpConstants.*

class ArtistController {

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		response.addIntHeader X_PAGINATION_TOTAL, Artist.count()
		render Artist.list(params) as GSON
	}

	def save() {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def artistInstance = new Artist(request.GSON)
		if (artistInstance.save(flush: true)) {
			respondCreated artistInstance
		} else {
			respondUnprocessableEntity artistInstance
		}
	}

	def show() {
		def artistInstance = Artist.get(params.id)
		if (artistInstance) {
			respondFound artistInstance
		} else {
			respondNotFound params.id
		}
	}

	def update() {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def artistInstance = Artist.get(params.id)
		if (!artistInstance) {
			respondNotFound params.id
			return
		}

		if (params.version != null) {
			if (artistInstance.version > params.long('version')) {
				respondConflict(artistInstance)
				return
			}
		}

		artistInstance.properties = request.GSON

		if (artistInstance.save(flush: true)) {
			respondUpdated artistInstance
		} else {
			respondUnprocessableEntity artistInstance
		}
	}

	def delete() {
		def artistInstance = Artist.get(params.id)
		if (!artistInstance) {
			respondNotFound params.id
			return
		}

		try {
			artistInstance.delete(flush: true)
			respondDeleted params.id
		} catch (DataIntegrityViolationException e) {
			respondNotDeleted params.id
		}
	}

	private boolean requestIsJson() {
		GSON.isJson(request)
	}

	private void respondFound(Artist artistInstance) {
		response.status = SC_OK
		render artistInstance as GSON
	}

	private void respondCreated(Artist artistInstance) {
		response.status = SC_CREATED
		response.addHeader LOCATION, createLink(action: 'show', id: artistInstance.id)
		render artistInstance as GSON
	}

	private void respondUpdated(Artist artistInstance) {
		response.status = SC_OK
		render artistInstance as GSON
	}

	private void respondUnprocessableEntity(Artist artistInstance) {
		def responseBody = [:]
		responseBody.errors = artistInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_UNPROCESSABLE_ENTITY
		render responseBody as GSON
	}

	private void respondNotFound(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	private void respondConflict(Artist artistInstance) {
		artistInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
				[message(code: 'artist.label', default: 'Artist')] as Object[],
				'Another user has updated this Artist while you were editing')
		def responseBody = [:]
		responseBody.errors = artistInstance.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_CONFLICT
		render responseBody as GSON
	}

	private void respondDeleted(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.deleted.message', args: [message(code: 'artist.label', default: 'Artist'), id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondNotDeleted(id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: 'artist.label', default: 'Artist'), id])
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
