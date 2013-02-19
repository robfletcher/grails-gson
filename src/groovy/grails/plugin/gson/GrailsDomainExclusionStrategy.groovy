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
		if (!domainClass) {
			// not a domain class - serialize everything
			false
		} else if (field.name == domainClass.identifier.name) {
			// always serialize the id
			false
		} else if (!(field.name in domainClass.persistentProperties.name)) {
			// skip non-persistent properties
			true
		} else {
			// skip if the non-owning side of a bidirectional property or we will get a stack overflow
			def persistentProperty = domainClass.getPersistentProperty(field.name)
			persistentProperty.isBidirectional() && !persistentProperty.isOwningSide()
		}
	}

	boolean shouldSkipClass(Class<?> aClass) {
		false
	}

	private GrailsDomainClass getDomainClassForType(Class type) {
		grailsApplication.getDomainClasses().find { it.clazz == type }
	}
}
