package org.perfectable.introspection;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

// adding testable would make circular dependency and this is really simple extension which should be in mockito
public class MockitoExtension implements TestInstancePostProcessor, AfterEachCallback {
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
		MockitoAnnotations.initMocks(testInstance);
	}

	@Override
	public void afterEach(TestExtensionContext context) {
		Mockito.validateMockitoUsage();
	}

}
