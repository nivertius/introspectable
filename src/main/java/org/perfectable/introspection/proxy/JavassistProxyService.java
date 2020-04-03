package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.Immutable;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.objenesis.ObjenesisStd;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

// THIS IS NOT A PUBLIC API: class must be public because of ServiceLoader
@SuppressWarnings("javadoc")
@Immutable
@AutoService(ProxyService.class)
public final class JavassistProxyService implements ProxyService {

	private static final ObjenesisStd OBJENESIS = new ObjenesisStd();
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.of(Feature.SUPERCLASS);

	@Override
	public boolean supportsFeature(Feature feature) {
		return SUPPORTED_FEATURES.contains(feature);
	}

	@Override
	public <I> I instantiate(ClassLoader classLoader, Class<?> baseClass, List<? extends Class<?>> interfaces,
							 InvocationHandler<? super MethodInvocation<I>> handler)
			throws UnsupportedFeatureException {
		checkArgument(!Modifier.isFinal(baseClass.getModifiers()));
		Class<I> proxyClass = createProxyClass(baseClass, interfaces);
		return instantiateProxyClass(proxyClass, handler);
	}

	@SuppressWarnings("unchecked")
	private static <I> Class<I> createProxyClass(Class<?> baseClass, List<? extends Class<?>> interfaces) {
		if (ProxyFactory.isProxyClass(baseClass)
			&& interfaces.stream().allMatch(testedInterface -> testedInterface.isAssignableFrom(baseClass))) {
			return (Class<I>) baseClass;
		}
		else {
			ProxyFactory factory = new ProxyFactory();
			if (!baseClass.getName().equals(Object.class.getName())) {
				factory.setSuperclass(baseClass);
			}
			Class<?>[] interfacesArray = interfaces.toArray(EMPTY_CLASS_ARRAY);
			factory.setInterfaces(interfacesArray);
			return (Class<I>) factory.createClass();
		}
	}

	private static <I> I instantiateProxyClass(Class<I> proxyClass,
											   InvocationHandler<? super MethodInvocation<I>> handler) {
		MethodHandler handlerAdapter = JavassistInvocationHandlerAdapter.adapt(handler);
		I proxy = OBJENESIS.newInstance(proxyClass);
		((Proxy) proxy).setHandler(handlerAdapter);
		return proxy;
	}

	private static final class JavassistInvocationHandlerAdapter<T> implements MethodHandler {
		private final InvocationHandler<? super MethodInvocation<T>> handler;

		static <X> JavassistInvocationHandlerAdapter<X> adapt(InvocationHandler<? super MethodInvocation<X>> handler) {
			return new JavassistInvocationHandlerAdapter<>(handler);
		}

		private JavassistInvocationHandlerAdapter(InvocationHandler<? super MethodInvocation<T>> handler) {
			this.handler = handler;
		}

		@Nullable
		@Override
		public Object invoke(@Nullable Object self, Method thisMethod, Method proceed,
							 @Nullable Object[] args)
			throws Throwable {
			requireNonNull(thisMethod);
			if (thisMethod.equals(ObjectMethods.FINALIZE)) {
				return null; // ignore proxy finalization
			}
			@SuppressWarnings("unchecked")
			T castedSelf = (T) self;
			@SuppressWarnings("unchecked")
			MethodInvocation<T> invocation = MethodInvocation.intercepted(thisMethod, castedSelf, args);
			return this.handler.handle(invocation);
		}
	}

}
