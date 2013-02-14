package grails.plugin.gson

import com.google.gson.*
import org.codehaus.groovy.grails.commons.*

/**
 * An exclusion strategy that instructs Gson to skip non-persistent properties of Grails domain classes.
 */
class GrailsDomainExclusionStrategy implements ExclusionStrategy {

	private final GrailsApplication grailsApplication

	GrailsDomainExclusionStrategy(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication
	}

	boolean shouldSkipField(FieldAttributes field) {
		def domainClass = getDomainClassForType(field.declaringClass)
		if (domainClass) {
			field.name != domainClass.identifier.name && !(field.name in domainClass.persistentProperties.name)
		} else {
			false
		}
	}

	boolean shouldSkipClass(Class<?> aClass) {
		false
	}

	private GrailsDomainClass getDomainClassForType(Class type) {
		grailsApplication.getDomainClasses().find { it.clazz == type }
	}
}
