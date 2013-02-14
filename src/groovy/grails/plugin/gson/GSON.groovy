package grails.plugin.gson

import org.codehaus.groovy.grails.web.converters.AbstractConverter
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.codehaus.groovy.grails.web.converters.Converter
import com.google.gson.stream.JsonWriter
import org.codehaus.groovy.grails.commons.ApplicationHolder

class GSON extends AbstractConverter<JsonWriter> {

    private target

    void setTarget(target) {
        this.target = target
    }

    void render(Writer out) {
        gsonFactory.createGson().toJson(target, out)
    }

    void render(HttpServletResponse response) {
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

    private GsonFactory getGsonFactory() {
        def grailsApplication = ApplicationHolder.application
        new GsonFactory(grailsApplication)
    }

}
