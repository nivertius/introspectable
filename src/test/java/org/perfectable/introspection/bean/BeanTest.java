package org.perfectable.introspection.bean;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.bean.BeanAssert.assertThat;

// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings({"nullness:initialization.static.field.uninitialized", "nullness:initialization.field.uninitialized"})
class BeanTest {
	private static final String MESSAGE_METHOD_CALLED = "Test method should not be called";
	public static final String NON_EXISTENT_PROPERTY_NAME = "nonExistentProperty";

	@Test
	void empty() {
		EmptySubject instance = new EmptySubject();
		Bean<EmptySubject> bean = Bean.from(instance);

		assertThat(bean)
			.hasTypeExactly(EmptySubject.class)
			.hasEmptyFieldProperties()
			.hasEmptyRelated()
			.doesntHaveProperty(NON_EXISTENT_PROPERTY_NAME)
			.hasContentsSameAs(instance)
			.hasConsistentCopyMechanism();
	}

	static class EmptySubject {
		Object nonPropertyMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}

	@Test
	void singleFieldWithoutValue() {
		SingleFieldSubject instance = new SingleFieldSubject();
		Bean<SingleFieldSubject> bean = Bean.from(instance);

		assertThat(bean)
			.hasTypeExactly(SingleFieldSubject.class)
			.hasFieldPropertiesWithNames(SingleFieldSubject.PROPERTY_ONE_NAME)
			.hasEmptyRelated()
			.doesntHaveProperty(NON_EXISTENT_PROPERTY_NAME)
			.hasContentsSameAs(instance)
			.hasConsistentCopyMechanism();
	}

	@Test
	void singleFieldWithValue() {
		SingleFieldSubject instance = new SingleFieldSubject();
		String oneValue = "testValue";
		instance.one = oneValue;
		Bean<SingleFieldSubject> bean = Bean.from(instance);

		assertThat(bean)
			.hasTypeExactly(SingleFieldSubject.class)
			.hasFieldPropertiesWithNames(SingleFieldSubject.PROPERTY_ONE_NAME)
			.hasRelated(oneValue)
			.doesntHaveProperty(NON_EXISTENT_PROPERTY_NAME)
			.hasContentsSameAs(instance)
			.hasConsistentCopyMechanism();

		assertThat(bean)
			.property(SingleFieldSubject.PROPERTY_ONE_NAME)
			.hasName(SingleFieldSubject.PROPERTY_ONE_NAME)
			.hasTypeExactly(String.class)
			.isWritable()
			.isReadable()
			.hasValueSameAs(oneValue);
	}

	@SuppressWarnings({"initialization.static.fields.uninitialized", "nullness:initialization.field.uninitialized"})
	static class SingleFieldSubject {
		static final String PROPERTY_ONE_NAME = "one";

		@SuppressWarnings("unused")
		private static String staticField; // SUPPRESS UnusedPrivateField

		@SuppressWarnings("unused")
		private String one;

		Object nonPropertyMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}

	@Test
	void singleGetter() {
		SingleGetterSubject instance = new SingleGetterSubject();
		Bean<SingleGetterSubject> bean = Bean.from(instance);

		assertThat(bean)
			.hasTypeExactly(SingleGetterSubject.class)
			.hasEmptyFieldProperties()
			.hasEmptyRelated()
			.doesntHaveProperty(NON_EXISTENT_PROPERTY_NAME)
			.hasContentsSameAs(instance)
			.hasConsistentCopyMechanism();

		assertThat(bean)
			.property(SingleGetterSubject.PROPERTY_ONE_NAME)
			.hasName(SingleGetterSubject.PROPERTY_ONE_NAME)
			.hasTypeExactly(String.class)
			.isNotWritable()
			.isReadable()
			.hasValueSameAs(SingleGetterSubject.VALUE_ONE);
	}

	static class SingleGetterSubject {
		static final String PROPERTY_ONE_NAME = "one";

		static final String VALUE_ONE = "oneTest";

		String getOne() {
			return VALUE_ONE;
		}

		Object nonPropertyMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}


	@Test
	void singleSetter() {
		SingleSetterSubject instance = new SingleSetterSubject();
		Bean<SingleSetterSubject> bean = Bean.from(instance);

		assertThat(bean)
			.hasTypeExactly(SingleSetterSubject.class)
			.hasEmptyFieldProperties()
			.hasEmptyRelated()
			.doesntHaveProperty(NON_EXISTENT_PROPERTY_NAME)
			.hasContentsSameAs(instance)
			.hasConsistentCopyMechanism();

		assertThat(bean)
			.property(SingleSetterSubject.PROPERTY_ONE_NAME)
			.hasName(SingleSetterSubject.PROPERTY_ONE_NAME)
			.hasTypeExactly(String.class)
			.isWritable()
			.isNotReadable();
	}

	static class SingleSetterSubject {
		static final String PROPERTY_ONE_NAME = "one";

		void setOne(String value) {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

		void setNotASetter() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

		Object nonPropertyMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}

}
