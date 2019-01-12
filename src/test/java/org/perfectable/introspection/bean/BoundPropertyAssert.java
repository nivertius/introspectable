package org.perfectable.introspection.bean;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Objects;

@SuppressWarnings("BooleanParameter")
final class BoundPropertyAssert<VALUE>
	extends AbstractObjectAssert<BoundPropertyAssert<VALUE>, BoundProperty<?, VALUE>> {
	private final Objects objects = Objects.instance();

	private BoundPropertyAssert(BoundProperty<?, VALUE> actual) {
		super(actual, BoundPropertyAssert.class);
	}

	public static <VALUE> BoundPropertyAssert<VALUE> assertThat(BoundProperty<?, VALUE> actual) {
		return new BoundPropertyAssert<>(actual);
	}

	public BoundPropertyAssert<VALUE> hasName(String expectedName) { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.name(), expectedName);
		return this;
	}

	public BoundPropertyAssert<VALUE> hasTypeExactly(Class<?> expectedType) { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.type(), expectedType);
		return this;
	}

	public BoundPropertyAssert<VALUE> isWritable() { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), true);
		return this;
	}

	public BoundPropertyAssert<VALUE> isNotWritable() { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), false);
		return this;
	}

	public BoundPropertyAssert<VALUE> isReadable() { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), true);
		return this;
	}

	public BoundPropertyAssert<VALUE> isNotReadable() { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), false);
		return this;
	}

	public BoundPropertyAssert<VALUE> hasNullValue() { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertNull(info, actual.get());
		return this;
	}

	public BoundPropertyAssert<VALUE> hasValueSameAs(VALUE expectedValue) { // SUPPRESS LinguisticNaming
		objects.assertNotNull(info, actual);
		objects.assertSame(info, actual.get(), expectedValue);
		return this;
	}
}
