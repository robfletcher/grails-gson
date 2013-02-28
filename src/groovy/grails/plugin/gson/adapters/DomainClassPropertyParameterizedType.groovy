package grails.plugin.gson.adapters

import java.lang.reflect.*
import groovy.transform.TupleConstructor
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * An implementation of `ParameterizedType` that uses information from a `GrailsDomainClassProperty`
 * to get information about types in a persistent collection.
 *
 * For example if a GORM class has a declaration:
 *
 *     static hasMany = [crew: Pirate]
 *
 * `getActualTypeArguments` will return `Type[Pirate]`
 *
 * In the case of a `Map` relationship such as:
 *
 *     Map crew
 *     static hasMany = [crew: Pirate]
 *
 * `getActualTypeArguments` will return `Type[String, Pirate]`.
 */
@TupleConstructor
class DomainClassPropertyParameterizedType implements ParameterizedType {

	final GrailsDomainClassProperty property

	static Type forProperty(GrailsDomainClassProperty property) {
		if (property.manyToMany || property.oneToMany) {
			new DomainClassPropertyParameterizedType(property)
		} else {
			property.type
		}
	}

	Type[] getActualTypeArguments() {
		def referencedType = property.referencedPropertyType
		if (Map.isAssignableFrom(property.type)) {
			[String, referencedType] as Type[]
		} else {
			[referencedType] as Type[]
		}
	}

	Type getRawType() {
		property.type
	}

	Type getOwnerType() {
		null
	}

	@Override
	String toString() {
		"$rawType.name<${actualTypeArguments.name.join(', ')}>"
	}
}
