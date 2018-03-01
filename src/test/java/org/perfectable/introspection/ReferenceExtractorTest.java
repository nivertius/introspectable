package org.perfectable.introspection;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceExtractorTest {

	@Test
	void testExtractFunction() {
		Method method = ReferenceExtractor.of(Subject.class)
			.extract(Subject::withResultNoArgument);

		assertThat(method)
			.isEqualTo(SubjectReflection.WITH_RESULT_NO_ARGUMENT);
	}

	@Test
	void testExtractNoReference() {
		ReferenceExtractor.FunctionReference<Subject, Object> reference = subject -> null;
		ReferenceExtractor<Subject> extractor = ReferenceExtractor.of(Subject.class);
		assertThatThrownBy(() -> extractor.extract(reference))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("No method was called");
	}

	@Test
	void testExtractMultipleMethods() {
		ReferenceExtractor.FunctionReference<Subject, Object> reference = subject -> {
			subject.withResultNoArgument();
			return subject.withResultNoArgument();
		};

		ReferenceExtractor<Subject> extractor = ReferenceExtractor.of(Subject.class);
		assertThatThrownBy(() -> extractor.extract(reference))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Multiple methods were called");
	}


}
