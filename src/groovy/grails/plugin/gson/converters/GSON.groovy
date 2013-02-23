package grails.plugin.gson.converters

import javax.servlet.http.*
import com.google.gson.*
import com.google.gson.stream.JsonWriter
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.converters.*
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

class GSON extends AbstractConverter<JsonWriter> {

	@Lazy
	private GsonBuilder gsonBuilder = {
		def applicationContext = ApplicationHolder.application.mainContext
		applicationContext.getBean('gsonBuilder', GsonBuilder)
	}()

	public static final String CACHED_GSON = 'grails.plugin.gson.CACHED_GSON_REQUEST_CONTENT'

	private target

	GSON() {}

	GSON(target) {
		setTarget(target)
	}

	static boolean isJson(HttpServletRequest request) {
		(request.contentType =~ /^(application|text)\/json\b/).asBoolean()
	}

	static JsonElement parse(HttpServletRequest request) {
		JsonElement json = request.getAttribute(CACHED_GSON)
		if (!json) {
			def requestBody = new BufferedReader(request.reader)
			json = new JsonParser().parse(requestBody)
			request.setAttribute(CACHED_GSON, json)
		}
		json
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
