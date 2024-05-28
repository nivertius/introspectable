package org.perfectable.introspection.proxy

import org.aopalliance.intercept.MethodInterceptor
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

@SuppressWarnings("HardCodedStringLiteral")
abstract class AbstractProxyServiceSpec extends Specification {
	protected abstract ProxyService createService()

	protected static final Method PACKAGE_GET_TITLE = Package.getDeclaredMethod("getImplementationTitle")
	protected static final Method CHAR_SEQUENCE_LENGTH = CharSequence.getDeclaredMethod("length")

	@Shared
	private final ProxyService service = createService()

	@Requires({ instance.service.supportsFeature(ProxyService.Feature.SUPERCLASS) })
	def 'class'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<Package>> handler = Mock()
		when:
		def proxy = ProxyBuilder.forClass(Package)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof Package

		when:
		def result = proxy.getImplementationTitle()

		then:
		1 * handler.handle({ invocation(it, PACKAGE_GET_TITLE, proxy) }) >> "test"

		and:
		result == "test"
	}

	@Requires({ instance.service.supportsFeature(ProxyService.Feature.SUPERCLASS) })
	def 'custom class with interface'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<Number>> handler = Mock()

		when:
		def proxy = ProxyBuilder.forClass(Package)
				.withInterface(CharSequence)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof Package
		proxy instanceof CharSequence

		when:
		def resultFirst = proxy.getImplementationTitle()

		then:
		1 * handler.handle({ invocation(it, PACKAGE_GET_TITLE, proxy) }) >> "test"

		and:
		resultFirst == "test"

		when:
		def resultSecond = ((CharSequence) proxy).length()

		then:
		1 * handler.handle({ invocation(it, CHAR_SEQUENCE_LENGTH, proxy) }) >> 23

		and:
		resultSecond == 23
	}

	def 'object class with interface'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<Package>> handler = Mock()
		when:
		def proxy = ProxyBuilder.forClass(Object)
				.withInterface(SimpleService)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof SimpleService

		when:
		def result = (proxy as SimpleService).process("value")

		then:
		1 * handler.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >> "first"

		and:
		result == "first"
	}

	def 'multiple interfaces'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<SimpleService>> handler = Mock()

		when:
		def proxy = ProxyBuilder.forInterface(SimpleService)
				.withInterface(CheckedService)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof SimpleService
		proxy instanceof CheckedService

		when:
		def resultFirst = proxy.process("value")

		then:
		1 * handler.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >> "first"

		and:
		resultFirst == "first"

		when:
		def resultSecond = ((CheckedService) proxy).checked()

		then:
		1 * handler.handle({ invocation(it, CheckedService.METHOD, proxy) }) >> "second"

		and:
		resultSecond == "second"
	}

	def 'interceptor'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<SimpleService>> handler = Mock()
		InvocationHandler<Object, Exception, MethodInvocation<SimpleService>> interceptor = Mock()

		when:
		def proxy = ProxyBuilder.forInterface(SimpleService)
				.withInterceptor(interceptor)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof SimpleService

		when:
		def resultFirst = proxy.process("value")

		then:
		1 * interceptor.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >>
				{ MethodInvocation<SimpleService> i -> i.invoke() }

		then:
		1 * handler.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >> "first"

		and:
		resultFirst == "first"

		when:
		def resultSecond = proxy.process("other")

		then:
		1 * interceptor.handle({ invocation(it, SimpleService.METHOD, proxy, "other") }) >> "second"
		0 * handler.handle(_)

		and:
		resultSecond == "second"
	}

	def 'aopalliance interceptor'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<SimpleService>> handler = Mock()
		MethodInterceptor interceptor = Mock()

		when:
		def proxy = ProxyBuilder.forInterface(SimpleService)
				.withAopInterceptor(interceptor)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof SimpleService

		when:
		def resultFirst = proxy.process("value")

		then:
		1 * interceptor.invoke({ aopinvocation(it, SimpleService.METHOD, proxy, "value") }) >>
				{ org.aopalliance.intercept.MethodInvocation i -> i.proceed() }

		then:
		1 * handler.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >> "first"

		and:
		resultFirst == "first"

		when:
		def resultSecond = proxy.process("other")

		then:
		1 * interceptor.invoke({ aopinvocation(it, SimpleService.METHOD, proxy, "other") }) >> "second"
		0 * handler.handle(_)

		and:
		resultSecond == "second"
	}

	def 'checked exception'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<CheckedService>> handler = Mock()
		def simulated = new IOException()

		when:
		def proxy = ProxyBuilder.forInterface(CheckedService)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof CheckedService

		when:
		proxy.checked()

		then:
		1 * handler.handle({ invocation(it, CheckedService.METHOD, proxy) }) >> { throw simulated }

		and:
		IOException e = thrown()
		e === simulated
	}

	def 'unchecked exception'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<SimpleService>> handler = Mock()
		def simulated = new RuntimeException()

		when:
		def proxy = ProxyBuilder.forInterface(SimpleService)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof SimpleService

		when:
		proxy.process("value")

		then:
		1 * handler.handle({ invocation(it, SimpleService.METHOD, proxy, "value") }) >> { throw simulated }

		and:
		RuntimeException e = thrown()
		e === simulated
	}

	def 'reference varargs'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<ReferenceVarService>> handler = Mock()
		def returned = new Object()

		when:
		def proxy = ProxyBuilder.forInterface(ReferenceVarService)
						.usingService(service)
						.instantiate(handler)

		then:
		proxy instanceof ReferenceVarService

		when:
		def result = proxy.process("one", "two", "three")

		then:
		1 * handler.handle({ invocation(it, ReferenceVarService.METHOD, proxy, "one", "two", "three") }) >> returned

		and:
		result == returned
	}

	def 'primitive varargs'() {
		setup:
		InvocationHandler<Object, Exception, MethodInvocation<PrimitiveVarService>> handler = Mock()
		def returned = new Object()

		when:
		def proxy = ProxyBuilder.forInterface(PrimitiveVarService)
				.usingService(service)
				.instantiate(handler)

		then:
		proxy instanceof PrimitiveVarService

		when:
		def result = proxy.process("one", 1, 2)

		then:
		1 * handler.handle({ invocation(it, PrimitiveVarService.METHOD, proxy, "one", 1, 2) }) >> returned

		and:
		result == returned
	}

	static <I> boolean invocation(MethodInvocation<I> invocation, Method method, I receiver, Object... arguments) {
		return invocation.decompose({ Method actualMethod, I actualReceiver, Object[] actualArguments ->
			method == actualMethod && receiver == actualReceiver && arguments == actualArguments
		})
	}

	static boolean aopinvocation(org.aopalliance.intercept.MethodInvocation invocation,
								 Method method, Object receiver, Object... arguments) {
		return invocation.method == method && invocation.getThis() == receiver && invocation.arguments == arguments;
	}

	interface SimpleService {
		Object process(String value)

		Method METHOD = SimpleService.getDeclaredMethod("process", String)
	}

	interface ReferenceVarService {
		Object process(String required, String... variable)

		Method METHOD = ReferenceVarService.getDeclaredMethod("process", String, String[])
	}

	interface PrimitiveVarService {
		Object process(String required, int... variable);

		Method METHOD = PrimitiveVarService.getDeclaredMethod("process", String, int[])
	}

	interface CheckedService {
		Object checked() throws IOException

		Method METHOD = CheckedService.getDeclaredMethod("checked")
	}
}
