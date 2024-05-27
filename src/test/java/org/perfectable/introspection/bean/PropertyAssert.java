package org.perfectable.introspection.bean;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("BooleanParameter")
final class PropertyAssert<VALUE>
	extends AbstractObjectAssert<PropertyAssert<VALUE>, Property<?, VALUE>> {
	@SuppressWarnings("HidingField")
	private final Objects objects = Objects.instance();

	private PropertyAssert(Property<?, VALUE> actual) {
		super(actual, PropertyAssert.class);
	}

	public static <VALUE> PropertyAssert<VALUE> assertThat(Property<?, VALUE> actual) {
		return new PropertyAssert<>(actual);
	}

	public PropertyAssert<VALUE> hasName(String expectedName) {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.name(), expectedName);
		return this;
	}

	public PropertyAssert<VALUE> hasTypeExactly(Class<?> expectedType) {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.type(), expectedType);
		return this;
	}

	public PropertyAssert<VALUE> isWritable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), true);
		return this;
	}

	public PropertyAssert<VALUE> isNotWritable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isWritable(), false);
		return this;
	}

	public PropertyAssert<VALUE> isReadable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), true);
		return this;
	}

	public PropertyAssert<VALUE> isNotReadable() {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.isReadable(), false);
		return this;
	}

	@SuppressWarnings("nullness:argument")
	public PropertyAssert<VALUE> hasNullValue() {
		objects.assertNotNull(info, actual);
		objects.assertNull(info, actual.get());
		return this;
	}

	@SuppressWarnings("nullness:argument")
	public PropertyAssert<VALUE> hasValueSameAs(@Nullable Object expectedValue) {
		objects.assertNotNull(info, actual);
		objects.assertSame(info, actual.get(), expectedValue);
		return this;
	}
}
