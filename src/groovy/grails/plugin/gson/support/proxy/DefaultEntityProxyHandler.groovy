package grails.plugin.gson.support.proxy

import org.codehaus.groovy.grails.support.proxy.*

/**
 * This is an extension of `DefaultProxyHandler` that makes it comply with the `EntityProxyHandler` interface.
 */
class DefaultEntityProxyHandler extends DefaultProxyHandler implements EntityProxyHandler {

	@Override
    Object getProxyIdentifier(Object o) {
		null
	}

	@Override
    Class<?> getProxiedClass(Object o) {
		o.getClass()
	}
}
