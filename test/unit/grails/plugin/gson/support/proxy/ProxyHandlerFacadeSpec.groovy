package grails.plugin.gson.support.proxy

import org.codehaus.groovy.grails.support.proxy.EntityProxyHandler
import org.grails.datastore.gorm.proxy.GroovyProxyFactory
import spock.lang.Specification
import spock.lang.IgnoreRest

class ProxyHandlerFacadeSpec extends Specification {

	ProxyHandlerFacade proxyFacade
	EntityProxyHandler proxyHandler
	GroovyProxyFactory proxyFactory

	def setup() {
		proxyHandler = Mock(DefaultEntityProxyHandler)
		proxyFactory = Mock(GroovyProxyFactory)
		proxyFacade = new ProxyHandlerFacade(proxyHandler)
		proxyFacade.proxyFactory = proxyFactory
	}

	@IgnoreRest
	def "facade should skip groovy proxy where metaClass is not accessible"() {

		when: "calling isGroovyProxy where no metaClass is accessible"
		def isGroovyProxy = proxyFacade.isGroovyProxy(stub)

		then: "it should return false"
		!isGroovyProxy

		and: "not delegate to the groovyProxyFactory"
		0 * proxyFactory.isProxy(_)

		where:
		stub << [null, [metaClass: null]]
	}

	def "getProxyIdentifier should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling getProxyIdentifier"
		proxyFacade.getProxyIdentifier(stub)

		then: "the correct delegate is being used"
		if (isGroovyProxy)
			1 * proxyFactory.getIdentifier(stub)
		else
			1 * proxyHandler.getProxyIdentifier(stub)

		where:
		isGroovyProxy | stub
		true          | new Object()
		false         | new Object()
	}

	def "getProxiedClass should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling getProxiedIdentifier"
		def result = proxyFacade.getProxiedClass(stub)

		then: "the correct delegate is being used"
		result == (isGroovyProxy ? String.class : null)
		(isGroovyProxy ? 0 : 1) * proxyHandler.getProxiedClass(stub)

		where:
		isGroovyProxy | stub
		true          | "stub"
		false         | "stub"
	}

	def "isProxy should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling isProxy"
		proxyFacade.isProxy(stub)

		then: "the correct delegate is being used"
		(isGroovyProxy ? 0 : 1) * proxyHandler.isProxy(stub)

		where:
		isGroovyProxy | stub
		true          | "stub"
		false         | "stub"
	}

	def "unwrapIfProxy should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling unwrapIfProxy"
		proxyFacade.unwrapIfProxy(stub)

		then: "the correct delegate is being used"
		if (isGroovyProxy)
			1 * proxyFactory.unwrap(stub)
		else
			1 * proxyHandler.unwrapIfProxy(stub)

		where:
		isGroovyProxy | stub
		true          | new Object()
		false         | new Object()
	}

	def "isInitialized should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling isInitialized"
		proxyFacade.isInitialized(stub)

		then: "the correct delegate is being used"
		if (isGroovyProxy)
			1 * proxyFactory.isInitialized(stub)
		else
			1 * proxyHandler.isInitialized(stub)

		where:
		isGroovyProxy | stub
		true          | new Object()
		false         | new Object()
	}

	def "initialize should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub) >> isGroovyProxy

		when: "calling initialize"
		proxyFacade.initialize(stub)

		then: "the correct delegate is being used"
		if (isGroovyProxy) stub.called
		else 1 * proxyHandler.initialize(stub)

		where:
		isGroovyProxy | stub
		true          | new InitializableStub()
		false         | new Object()
	}

	def "isInitialized[association] should handle groovy proxies"() {
		setup: "mocks"
		1 * proxyFactory.isProxy(stub.association) >> isGroovyProxy

		when: "calling isInitialized"
		proxyFacade.isInitialized(stub, "association")

		then: "the correct delegate is being used"
		if (isGroovyProxy)
			1 * proxyFactory.isInitialized(stub.association)
		else
			1 * proxyHandler.isInitialized(stub, "association")

		where:
		isGroovyProxy | stub
		true          | [association: true]
		false         | [association: true]
	}
}

class InitializableStub {
	def called = false
	def initialize() { called = true }
}
