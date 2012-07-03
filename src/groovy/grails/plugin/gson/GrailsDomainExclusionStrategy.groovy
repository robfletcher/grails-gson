package grails.plugin.gson

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import com.google.gson.*

class GrailsDomainExclusionStrategy implements ExclusionStrategy {

    private final GrailsDomainClass domainClass
	private final Collection<String> persistentPropertyNames

    GrailsDomainExclusionStrategy(GrailsDomainClass domainClass) {
        this.domainClass = domainClass
		persistentPropertyNames = (domainClass.persistentProperties.name).asImmutable()
    }

    boolean shouldSkipField(FieldAttributes field) {
        !isPersistentProperty(field.name)
    }

	boolean shouldSkipClass(Class<?> aClass) {
        false
    }

	private boolean isPersistentProperty(String name) {
		name in persistentPropertyNames
	}
}
