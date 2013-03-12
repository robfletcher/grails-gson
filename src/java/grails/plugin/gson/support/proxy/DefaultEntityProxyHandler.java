package grails.plugin.gson.support.proxy;

import org.codehaus.groovy.grails.support.proxy.*;

/**
 * This is an extension of `DefaultProxyHandler` that makes it comply with the `EntityProxyHandler` interface.
 */
public class DefaultEntityProxyHandler extends DefaultProxyHandler implements EntityProxyHandler {

	@Override
    public Object getProxyIdentifier(Object o) {
		return null;
	}

	@Override
    public Class<?> getProxiedClass(Object o) {
		return o.getClass();
	}
}
