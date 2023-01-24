package org.perfectable.introspection.query;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Iterables;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Preconditions.checkArgument;

final class AbstractQueryAssert<ELEMENT extends @NonNull Object, QUERY extends AbstractQuery<ELEMENT, QUERY>>
	extends AbstractObjectAssert<AbstractQueryAssert<ELEMENT, QUERY>, QUERY> {

	private final Iterables iterables = Iterables.instance();
	private final Predicate<? super ELEMENT> filter;

	private AbstractQueryAssert(QUERY actual, Predicate<? super ELEMENT> filter) {
		super(actual, AbstractQueryAssert.class);
		this.filter = filter;
	}

	static <ELEMENT extends @NonNull Object, QUERY extends AbstractQuery<ELEMENT, QUERY>> 
			AbstractQueryAssert<ELEMENT, QUERY> assertThat(QUERY actual) {
		return new AbstractQueryAssert<>(actual, element -> true);
	}

	AbstractQueryAssert<ELEMENT, QUERY> isEmpty() {
		isNotNull();
		iterables.assertEmpty(info, actual);
		if (actual.isPresent()) {
			failWithMessage("Expected query not to have present element");
		}
		Optional<? extends ELEMENT> option = actual.option();
		if (option.isPresent()) {
			ELEMENT optionValue = option.get();
			failWithMessage("Expected query option not to be present, but contained <%s>", optionValue);
		}
		checkUniqueThrows(NoSuchElementException.class);
		return myself;
	}

	AbstractQueryAssert<ELEMENT, QUERY> isSingleton(ELEMENT onlyElement) {
		isNotNull();
		containsExactly(onlyElement);
		hasOption(onlyElement);
		hasUnique(onlyElement);
		return myself;
	}

	@SuppressWarnings("nullness:argument")
	AbstractQueryAssert<ELEMENT, QUERY> hasOption(ELEMENT onlyElement) {
		checkOptionPresent();
		Optional<? extends ELEMENT> option = actual.option();
		ELEMENT optionValue = option.get();
		if (!Objects.equals(optionValue, onlyElement)) {
			failWithMessage("Expected query option to be equal to <%s>, but was <%s>", onlyElement, optionValue);
		}
		return myself;
	}

	@SuppressWarnings("argument.type.incompatible")
	AbstractQueryAssert<ELEMENT, QUERY> hasUnique(ELEMENT onlyElement) {
		isNotNull();
		try {
			@Nullable ELEMENT uniqueResult = this.actual.unique();
			if (!Objects.equals(uniqueResult, onlyElement)) {
				failWithMessage("Expected query unique to be equal to <%s>, but was <%s>", onlyElement, uniqueResult);
			}
		}
		catch (Exception e) { // SUPPRESS IllegalCatch
			failWithMessage("Expected query unique not to throw", e);
		}
		return myself;
	}

	AbstractQueryAssert<ELEMENT, QUERY> doesNotHaveDuplicates() {
		iterables.assertDoesNotHaveDuplicates(info, actual);
		return myself;
	}

	@SafeVarargs
	@SuppressWarnings({"varargs", "nullness:argument"})
	final AbstractQueryAssert<ELEMENT, QUERY> contains(@Nullable ELEMENT... elements) {
		checkArgument(elements.length > 0, "use isEmpty instead"); // SUPPRESS MultipleStringLiterals
		List<? extends ELEMENT> mapped = collectElements();
		iterables.assertContains(info, mapped, elements);
		checkContains(elements);
		return myself;
	}

	@SafeVarargs
	@SuppressWarnings({"varargs", "nullness:argument"})
	final AbstractQueryAssert<ELEMENT, QUERY> containsExactly(@Nullable ELEMENT... elements) {
		checkArgument(elements.length > 0, "use isEmpty instead"); // SUPPRESS MultipleStringLiterals
		List<? extends ELEMENT> mapped = collectElements();
		iterables.assertContainsExactlyInAnyOrder(info, mapped, elements);
		checkContains(elements);
		return myself;
	}

	@SuppressWarnings({"varargs", "nullness:argument"})
	AbstractQueryAssert<ELEMENT, QUERY> sortsCorrectlyWith(Comparator<? super ELEMENT> comparator) {
		Object[] expected = actual.stream().filter(filter).sorted(comparator).toArray();
		QUERY tested = actual.sorted(comparator).filter(filter);
		iterables.assertContainsExactly(info, tested, expected);
		return myself;
	}


	@SuppressWarnings("nullness:argument")
	AbstractQueryAssert<ELEMENT, QUERY> doesNotContain(@Nullable Object... elements) {
		iterables.assertDoesNotContain(info, actual, elements);
		for (@Nullable Object element : elements) {
			if (actual.contains(element)) {
				failWithMessage("Expected query to not contain <%s> but did", element);
			}
		}
		return myself;
	}

	AbstractQueryAssert<ELEMENT, QUERY> filteredOn(Predicate<? super ELEMENT> nextFilter) {
		@SuppressWarnings("unchecked")
		Predicate<? super ELEMENT> newFilter = ((Predicate<ELEMENT>) filter).and(nextFilter);
		return new AbstractQueryAssert<>(actual, newFilter);
	}

	private void checkOptionPresent() {
		isNotNull();
		if (!actual.isPresent()) {
			failWithMessage("Expected query to have present element");
		}
		Optional<? extends ELEMENT> option = actual.option();
		if (!option.isPresent()) {
			failWithMessage("Expected query option to be present");
		}
	}

	@SuppressWarnings("nullness:argument")
	private void checkUniqueThrows(Class<? extends Throwable> exceptionClass) {
		try {
			ELEMENT uniqueResult = this.actual.unique();
			failWithMessage("Expected query unique to throw, but it returned <%s>", uniqueResult);
		}
		catch (Exception e) { // SUPPRESS IllegalCatch
			if (!exceptionClass.isInstance(e)) {
				failWithMessage("Expected query unique to throw " + exceptionClass.getSimpleName(), e);
			}
		}
	}

	@SuppressWarnings("nullness:argument")
	@SafeVarargs
	private final void checkContains(@Nullable ELEMENT... elements) {
		if (elements.length > 2) { // SUPPRESS AvoidLiteralsInIfCondition
			checkUniqueThrows(IllegalArgumentException.class);
		}
		for (@Nullable ELEMENT element : elements) {
			if (!actual.contains(element)) {
				failWithMessage("Expected query to contain <%s> but didn't", element);
			}
		}
	}

	private List<? extends ELEMENT> collectElements() {
		return actual.stream().filter(filter).collect(toList());
	}
}
