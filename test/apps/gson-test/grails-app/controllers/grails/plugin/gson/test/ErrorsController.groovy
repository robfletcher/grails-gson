package grails.plugin.gson.test

import grails.plugin.gson.converters.GSON
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

class ErrorsController {

	def notFound() {
		def responseBody = [:]
		responseBody.message = request.exception.message
		response.status = SC_NOT_FOUND
		render responseBody as GSON
	}

	def serverError() {
		def responseBody = [:]
		responseBody.message = request.exception.message
		response.status = SC_INTERNAL_SERVER_ERROR
		render responseBody as GSON
	}

}
