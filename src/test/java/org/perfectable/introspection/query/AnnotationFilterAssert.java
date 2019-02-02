package org.perfectable.introspection.query;

import java.lang.reflect.AnnotatedElement;

import org.assertj.core.api.AbstractObjectAssert;

final class AnnotationFilterAssert extends AbstractObjectAssert<AnnotationFilterAssert, AnnotationFilter> {

	private AnnotationFilterAssert(AnnotationFilter typeFilter) {
		super(typeFilter, AnnotationFilterAssert.class);
	}

	static AnnotationFilterAssert assertThat(AnnotationFilter typeFilter) {
		return new AnnotationFilterAssert(typeFilter);
	}

	AnnotationFilterAssert matchesElement(AnnotatedElement testedElement) {
		isNotNull();
		if (!actual.matches(testedElement)) {
			failWithMessage("Expected filter to match element <%s>", testedElement);
		}
		if (actual.negated().matches(testedElement)) {
			failWithMessage("Expected filter negation not to match element <%s>", testedElement);
		}
		return myself;
	}

	AnnotationFilterAssert doesntMatchElement(AnnotatedElement testedElement) {
		isNotNull();
		if (actual.matches(testedElement)) {
			failWithMessage("Expected filter not to match element <%s>", testedElement);
		}
		if (!actual.negated().matches(testedElement)) {
			failWithMessage("Expected filter negation to match element <%s>", testedElement);
		}
		return myself;
	}
}
