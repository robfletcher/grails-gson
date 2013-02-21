<%=packageName ? "package ${packageName}\n\n" : ''%>import grails.plugin.gson.converters.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*
import static grails.plugin.gson.http.HttpConstants.*

class ${className}Controller {

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		response.addIntHeader X_PAGINATION_TOTAL, ${className}.count()
		render ${className}.list(params) as GSON
	}

	def save() {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def ${propertyName} = new ${className}(request.GSON)
		if (${propertyName}.save(flush: true)) {
			respondCreated ${propertyName}
		} else {
			respondUnprocessableEntity ${propertyName}
		}
	}

	def show(Long id) {
		def ${propertyName} = ${className}.get(id)
		if (${propertyName}) {
			respondFound ${propertyName}
		} else {
			respondNotFound id
		}
	}

	def update(Long id, Long version) {
		if (!requestIsJson()) {
			respondNotAcceptable()
			return
		}

		def ${propertyName} = ${className}.get(id)
		if (!${propertyName}) {
			respondNotFound id
			return
		}

		if (version != null) {
			if (${propertyName}.version > version) {
				respondConflict(${propertyName})
				return
			}
		}

		${propertyName}.properties = request.GSON

		if (${propertyName}.save(flush: true)) {
			respondUpdated ${propertyName}
		} else {
			respondUnprocessableEntity ${propertyName}
		}
	}

	def delete(Long id) {
		def ${propertyName} = ${className}.get(id)
		if (!${propertyName}) {
			respondNotFound id
			return
		}

		try {
			${propertyName}.delete(flush: true)
			respondDeleted id
		} catch (DataIntegrityViolationException e) {
			respondNotDeleted id
		}
	}

	private boolean requestIsJson() {
		request.contentType == 'application/json'
	}

	private void respondFound(${className} ${propertyName}) {
		response.status = SC_OK
		render ${propertyName} as GSON
	}

	private void respondCreated(${className} ${propertyName}) {
		response.status = SC_CREATED
		render ${propertyName} as GSON
	}

	private void respondUpdated(${className} ${propertyName}) {
		response.status = SC_OK
		render ${propertyName} as GSON
	}

	private void respondUnprocessableEntity(${className} ${propertyName}) {
		def responseBody = [:]
		responseBody.errors = ${propertyName}.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_UNPROCESSABLE_ENTITY
		render responseBody as GSON
	}

	private void respondNotFound(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	private void respondConflict(${className} ${propertyName}) {
		${propertyName}.errors.rejectValue('version', 'default.optimistic.locking.failure',
				[message(code: '${domainClass.propertyName}.label', default: '${className}')] as Object[],
				'Another user has updated this ${className} while you were editing')
		def responseBody = [:]
		responseBody.errors = ${propertyName}.errors.allErrors.collect {
			message(error: it)
		}
		response.status = SC_CONFLICT
		render responseBody as GSON
	}

	private void respondDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
		response.status = SC_OK
		render responseBody as GSON
	}

	private void respondNotDeleted(long id) {
		def responseBody = [:]
		responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), id])
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
