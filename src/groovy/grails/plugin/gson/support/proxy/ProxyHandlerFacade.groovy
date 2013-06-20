package grails.plugin.gson.support.proxy

import groovy.transform.TupleConstructor
import org.codehaus.groovy.grails.support.proxy.EntityProxyHandler
import org.grails.datastore.gorm.proxy.GroovyProxyFactory

/**
 * Supports proxies for hibernate as well as mongodb mapped domain instances.
 * As there may only be one active ProxyHandler bean defined this facade does not implement (Entity-)ProxyHandler.
 */
@TupleConstructor
class ProxyHandlerFacade {

	final EntityProxyHandler proxyHandler
	
	protected GroovyProxyFactory proxyFactory = new GroovyProxyFactory()

	Object getProxyIdentifier(Object o) {
		if (isGroovyProxy(o))
			proxyFactory.getIdentifier o
		else
			proxyHandler.getProxyIdentifier o
	}

	boolean isGroovyProxy(o) {
		o?.metaClass != null && proxyFactory.isProxy(o)
	}

	Class<?> getProxiedClass(Object o) {
		if (isGroovyProxy(o))
			o.class
		else
			proxyHandler.getProxiedClass o
	}

	boolean isProxy(Object o) {
		isGroovyProxy(o) || proxyHandler.isProxy(o)
	}

	Object unwrapIfProxy(Object instance) {
		if (isGroovyProxy(instance))
			proxyFactory.unwrap instance
		else
			proxyHandler.unwrapIfProxy instance
	}

	boolean isInitialized(Object o) {
		if (isGroovyProxy(o))
			proxyFactory.isInitialized o
		else
			proxyHandler.isInitialized o
	}

	void initialize(Object o) {
		if (isGroovyProxy(o))
			o.initialize()
		else
			proxyHandler.initialize o
	}

	boolean isInitialized(Object obj, String associationName) {
		if (isProxy(obj, associationName))
			proxyFactory.isInitialized obj[associationName]
		else
			proxyHandler.isInitialized obj, associationName
	}

	boolean isProxy(Object obj, String associationName) {
		obj != null && isGroovyProxy(obj[associationName])
	}
}
