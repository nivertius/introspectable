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

	public BoundPropertyAssert<VALUE> hasName(String expectedName) {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.name(), expectedName);
		return this;
	}

	public BoundPropertyAssert<VALUE> hasTypeExactly(Class<?> expectedType) {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.type(), expectedType);
		return this;
	}

	public BoundPropertyAssert<VALUE> isWritable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), true);
		return this;
	}

	public BoundPropertyAssert<VALUE> isNotWritable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), false);
		return this;
	}

	public BoundPropertyAssert<VALUE> isReadable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), true);
		return this;
	}

	public BoundPropertyAssert<VALUE> isNotReadable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), false);
		return this;
	}

	public BoundPropertyAssert<VALUE> hasNullValue() {
		objects.assertNotNull(info, actual);
		objects.assertNull(info, actual.get());
		return this;
	}

	public BoundPropertyAssert<VALUE> hasValueSameAs(VALUE expectedValue) {
		objects.assertNotNull(info, actual);
		objects.assertSame(info, actual.get(), expectedValue);
		return this;
	}
}
