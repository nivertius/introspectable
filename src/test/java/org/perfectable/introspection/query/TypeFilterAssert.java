package org.perfectable.introspection.query;

import org.assertj.core.api.AbstractObjectAssert;

final class TypeFilterAssert extends AbstractObjectAssert<TypeFilterAssert, TypeFilter> {

	private TypeFilterAssert(TypeFilter typeFilter) {
		super(typeFilter, TypeFilterAssert.class);
	}

	static TypeFilterAssert assertThat(TypeFilter typeFilter) {
		return new TypeFilterAssert(typeFilter);
	}

	TypeFilterAssert matchesType(Class<?> testedClass) {
		isNotNull();
		if (!actual.matches(testedClass)) {
			failWithMessage("Expected filter to match class <%s>", testedClass.getName());
		}
		if (actual.negated().matches(testedClass)) {
			failWithMessage("Expected filter negation not to match class <%s>", testedClass.getName());
		}
		return myself;
	}

	TypeFilterAssert doesntMatchType(Class<?> testedClass) {
		isNotNull();
		if (actual.matches(testedClass)) {
			failWithMessage("Expected filter not to match class <%s>", testedClass.getName());
		}
		if (!actual.negated().matches(testedClass)) {
			failWithMessage("Expected filter negation to match class <%s>", testedClass.getName());
		}
		return myself;
	}
}
