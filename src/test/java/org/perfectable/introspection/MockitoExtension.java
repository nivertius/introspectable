package org.perfectable.introspection;

import java.lang.reflect.Parameter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

// adding testable would make circular dependency and this is really simple extension which should be in mockito
public class MockitoExtension implements TestInstancePostProcessor, ParameterResolver,
			AfterEachCallback, AfterAllCallback {
	private static final ExtensionContext.Namespace NAMESPACE =
		ExtensionContext.Namespace.create(MockitoExtension.class);
	private static final Object MOCKS_CLOSER_KEY = new Object();

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
		AutoCloseable mockCloser = MockitoAnnotations.openMocks(testInstance);
		context.getStore(NAMESPACE).put(MOCKS_CLOSER_KEY, mockCloser);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Mockito.validateMockitoUsage();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		context.getStore(NAMESPACE).get(MOCKS_CLOSER_KEY, AutoCloseable.class).close();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		return parameterContext.getParameter().isAnnotationPresent(Mock.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		Parameter parameter = parameterContext.getParameter();
		@SuppressWarnings("nullness:cast.unsafe")
		Object casted = (@NonNull Object) Mockito.mock(parameter.getType(), parameter.getName());
		return casted;
	}
}
