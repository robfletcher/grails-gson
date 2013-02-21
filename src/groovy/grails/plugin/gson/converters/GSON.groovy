package grails.plugin.gson.converters

import javax.servlet.http.HttpServletResponse
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonWriter
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.converters.*
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

class GSON extends AbstractConverter<JsonWriter> {

	@Lazy private GsonBuilder gsonBuilder = {
		def applicationContext = ApplicationHolder.application.mainContext
		applicationContext.getBean('gsonBuilder', GsonBuilder)
	}()

	private target

	GSON() {}

	GSON(target) {
		setTarget(target)
	}

	void setTarget(target) {
		this.target = target
	}

	void render(Writer out) {
		try {
			gsonBuilder.create().toJson(target, out)
		} finally {
			out.flush()
			out.close()
		}
	}

	void render(HttpServletResponse response) {
		response.contentType = GrailsWebUtil.getContentType('application/json', 'UTF-8')
		render response.writer
	}

	JsonWriter getWriter() {
		throw new UnsupportedOperationException()
	}

	void convertAnother(Object o) {
		throw new UnsupportedOperationException()
	}

	void build(Closure c) {
		throw new UnsupportedOperationException()
	}

	ObjectMarshaller<? extends Converter> lookupObjectMarshaller(Object target) {
		throw new UnsupportedOperationException()
	}

}
