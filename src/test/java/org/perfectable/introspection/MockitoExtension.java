package org.perfectable.introspection;

import javax.annotation.Nullable;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

// adding testable would make circular dependency and this is really simple extension which should be in mockito
public class MockitoExtension implements TestInstancePostProcessor, AfterEachCallback {
	@Override
	public void postProcessTestInstance(@Nullable Object testInstance, @Nullable ExtensionContext context) {
		MockitoAnnotations.initMocks(testInstance);
	}

	@Override
	public void afterEach(@Nullable ExtensionContext context) {
		Mockito.validateMockitoUsage();
	}

}
