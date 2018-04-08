package org.perfectable.introspection.bean;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.Objects;
import org.assertj.core.internal.TypeComparators;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.internal.TypeComparators.defaultTypeComparators;

final class BeanAssert<ELEMENT>
	extends AbstractObjectAssert<BeanAssert<ELEMENT>, Bean<ELEMENT>> {

	private final Objects objects = Objects.instance();
	private final Iterables iterables = Iterables.instance();
	private final Map<String, Comparator<?>> comparatorByPropertyOrField = new TreeMap<>();
	private final TypeComparators comparatorByType = defaultTypeComparators();

	private BeanAssert(Bean<ELEMENT> actual) {
		super(actual, BeanAssert.class);
	}

	public static <ELEMENT> BeanAssert<ELEMENT> assertThat(Bean<ELEMENT> actual) {
		return new BeanAssert<>(actual);
	}

	public BeanAssert<ELEMENT> hasEmptyFieldProperties() {
		objects.assertNotNull(info, actual);
		iterables.assertEmpty(info, actual.fieldProperties());
		return this;
	}

	public BeanAssert<ELEMENT> hasFieldPropertiesWithNames(String... names) {
		objects.assertNotNull(info, actual);
		List<String> fieldPropertiesNames =
			actual.fieldProperties().stream().map(BoundProperty::name).collect(toList());
		iterables.assertContains(info, fieldPropertiesNames, names);
		return this;
	}

	public BeanAssert<ELEMENT> hasTypeExactly(Class<?> expectedType) {
		objects.assertNotNull(info, actual);
		objects.assertEqual(info, actual.type(), expectedType);
		return this;
	}

	public BeanAssert<ELEMENT> hasEmptyRelated() {
		objects.assertNotNull(info, actual);
		iterables.assertEmpty(info, actual.related());
		return this;
	}

	public BeanAssert<ELEMENT> hasRelated(Object... expectedRelated) {
		objects.assertNotNull(info, actual);
		iterables.assertContains(info, actual.related(), expectedRelated);
		return this;
	}

	public BeanAssert<ELEMENT> doesntHaveProperty(String propertyName) {
		objects.assertNotNull(info, actual);
		Class<ELEMENT> type = actual.type();
		assertThatThrownBy(() -> actual.property(propertyName))
			.hasMessage("No property " + propertyName + " for " + type);
		return this;
	}


	public BoundPropertyAssert<Object> property(String one) {
		objects.assertNotNull(info, actual);
		BoundProperty<ELEMENT, Object> property = actual.property(one);
		objects.assertNotNull(info, property);
		BoundPropertyAssert<Object> propertyAssert = BoundPropertyAssert.assertThat(property);
		return propertyAssert;
	}

	public BeanAssert<ELEMENT> hasContentsSameAs(ELEMENT expectedContents) {
		objects.assertNotNull(info, actual);
		objects.assertSame(info, actual.contents(), expectedContents);
		return this;
	}

	public BeanAssert<ELEMENT> hasConsistentCopyMechanism() {
		objects.assertNotNull(info, actual);
		Bean<ELEMENT> copy = actual.copy();
		objects.assertNotEqual(info, copy, actual);
		objects.assertNotSame(info, copy.contents(), actual.contents());
		objects.assertIsEqualToIgnoringGivenFields(info, copy.contents(), actual.contents(),
			comparatorByPropertyOrField, comparatorByType);
		return this;
	}

}
