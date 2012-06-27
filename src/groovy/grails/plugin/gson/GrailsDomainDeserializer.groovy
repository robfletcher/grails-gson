package grails.plugin.gson

import java.lang.reflect.Type
import com.google.gson.*

/**
 * A deserializer that works on Grails domain objects. If the JSON element contains an _id_ property then the domain
 * instance is retrieved from the database, otherwise a new instance is constructed. This means you can deserialize a
 * JSON HTTP request into a new domain instance or an update to an existing one.
 */
class GrailsDomainDeserializer implements JsonDeserializer {

    Object deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
        def jsonObject = element.getAsJsonObject()
        def id = context.deserialize(jsonObject.get('id'), type.metaClass.getMetaProperty('id').type)
        def instance = id ? type.get(id) : type.newInstance()
        for (prop in jsonObject.entrySet()) {
            instance.properties[prop.key] = context.deserialize(prop.value, instance.metaClass.getMetaProperty(prop.key).type)
        }
        instance
    }

}
