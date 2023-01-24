package org.perfectable.introspection;

import java.lang.reflect.Parameter;

import org.checkerframework.checker.nullness.qual.NonNull;
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
public class MockitoExtension implements TestInstancePostProcessor, ParameterResolver, AfterEachCallback {
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
		MockitoAnnotations.initMocks(testInstance);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Mockito.validateMockitoUsage();
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
