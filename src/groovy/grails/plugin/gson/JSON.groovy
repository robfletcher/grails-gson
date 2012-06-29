package grails.plugin.gson

import org.codehaus.groovy.grails.web.converters.AbstractConverter
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.codehaus.groovy.grails.web.converters.Converter
import com.google.gson.stream.JsonWriter

class JSON extends AbstractConverter<JsonWriter> {
	@Override
	void setTarget(Object target) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	void render(Writer out) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	void render(HttpServletResponse response) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	JsonWriter getWriter() {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	void convertAnother(Object o) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	void build(Closure c) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	ObjectMarshaller<? extends Converter> lookupObjectMarshaller(Object target) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}
}
