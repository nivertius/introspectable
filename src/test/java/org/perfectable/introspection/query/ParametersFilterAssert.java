package org.perfectable.introspection.query;

import java.lang.reflect.Method;

import org.assertj.core.api.AbstractObjectAssert;

final class ParametersFilterAssert extends AbstractObjectAssert<ParametersFilterAssert, ParametersFilter> {

	private ParametersFilterAssert(ParametersFilter parametersFilter) {
		super(parametersFilter, ParametersFilterAssert.class);
	}

	static ParametersFilterAssert assertThat(ParametersFilter parametersFilter) {
		return new ParametersFilterAssert(parametersFilter);
	}

	ParametersFilterAssert matchesMethod(Method testedMethod) {
		isNotNull();
		if (!actual.matches(testedMethod)) {
			failWithMessage("Expected filter to match class <%s>", testedMethod);
		}
		return myself;
	}

	ParametersFilterAssert doesntMatchMethod(Method testedMethod) {
		isNotNull();
		if (actual.matches(testedMethod)) {
			failWithMessage("Expected filter not to match method <%s>", testedMethod);
		}
		return myself;
	}
}
