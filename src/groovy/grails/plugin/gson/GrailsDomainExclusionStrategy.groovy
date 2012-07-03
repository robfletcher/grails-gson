package grails.plugin.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class GrailsDomainExclusionStrategy implements ExclusionStrategy {

    GrailsDomainClass domainClass

    GrailsDomainExclusionStrategy(GrailsDomainClass domainClass) {
        this.domainClass = domainClass
    }

    boolean shouldSkipField(FieldAttributes fieldAttributes) {
        println "is $fieldAttributes.name in $domainClass.persistentProperties.name"
        !(fieldAttributes.name in domainClass.persistentProperties.name)
    }

    boolean shouldSkipClass(Class<?> aClass) {
        false
    }
}
