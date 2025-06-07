package org.perfectable.introspection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class AnnotationBuilderExamples {

	@Retention(RetentionPolicy.RUNTIME)
	@interface Marker {}

	@Marker
	static class MarkerHolder {
		static final Marker HELD = MarkerHolder.class.getAnnotation(Marker.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Single {
		String value();
	}

	@Single(SingleHolder.VALUE)
	private static class SingleHolder {
		static final String VALUE = "testValue";
		static final Single HELD = SingleHolder.class.getAnnotation(Single.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Multiple {
		String DEFAULT_ONE = "defaultOne";
		String one() default DEFAULT_ONE;
		String two();
		int three();
	}

	@Multiple(two = MultipleHolder.TWO_VALUE, three = MultipleHolder.TRHEE_VALUE)
	private static class MultipleHolder {
		static final String TWO_VALUE = "testValue";
		static final int TRHEE_VALUE = 3;
		static final Multiple HELD = MultipleHolder.class.getAnnotation(Multiple.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ArrayElements {
		String[] value() default {};
	}

	@ArrayElements({"one", "two", "three"})
	private static class ArrayElementsHolder {
		static final String[] VALUE = new String[] {"one", "two", "three"};
		static final ArrayElements HELD = ArrayElementsHolder.class.getAnnotation(ArrayElements.class);
	}

}
