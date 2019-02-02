package org.perfectable.introspection.query;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Iterables;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Preconditions.checkArgument;

final class AbstractQueryAssert<ELEMENT, MAPPED>
	extends AbstractObjectAssert<AbstractQueryAssert<ELEMENT, MAPPED>, AbstractQuery<? extends ELEMENT, ?>> {

	private final Iterables iterables = Iterables.instance();
	private final Function<ELEMENT, ? extends MAPPED> mapper;
	private final Predicate<? super ELEMENT> filter;

	private AbstractQueryAssert(AbstractQuery<? extends ELEMENT, ?> actual,
								Function<ELEMENT, ? extends MAPPED> mapper, Predicate<? super ELEMENT> filter) {
		super(actual, AbstractQueryAssert.class);
		this.mapper = mapper;
		this.filter = filter;
	}

	static <ELEMENT> AbstractQueryAssert<ELEMENT, ELEMENT> assertThat(AbstractQuery<? extends ELEMENT, ?> actual) {
		return new AbstractQueryAssert<>(actual, Function.identity(), element -> true);
	}

	AbstractQueryAssert<ELEMENT, MAPPED> isEmpty() {
		isNotNull();
		iterables.assertEmpty(info, actual);
		if (actual.isPresent()) {
			failWithMessage("Expected query not to have present element");
		}
		Optional<? extends ELEMENT> option = actual.option();
		if (option.isPresent()) {
			MAPPED optionValue = option.map(mapper).get();
			failWithMessage("Expected query option not to be present, but contained <%s>", optionValue);
		}
		checkUniqueThrows(NoSuchElementException.class);
		return myself;
	}

	AbstractQueryAssert<ELEMENT, MAPPED> isSingleton(MAPPED onlyElement) {
		isNotNull();
		containsExactly(onlyElement);
		hasOption(onlyElement);
		hasUnique(onlyElement);
		return myself;
	}

	AbstractQueryAssert<ELEMENT, MAPPED> hasOption(MAPPED onlyElement) {
		checkOptionPresent();
		Optional<? extends ELEMENT> option = actual.option();
		MAPPED optionValue = option.map(mapper).get();
		if (!Objects.equals(optionValue, onlyElement)) {
			failWithMessage("Expected query option to be equal to <%s>, but was <%s>", onlyElement, optionValue);
		}
		return myself;
	}

	AbstractQueryAssert<ELEMENT, MAPPED> hasUnique(MAPPED onlyElement) {
		isNotNull();
		try {
			ELEMENT uniqueResult = this.actual.unique();
			MAPPED uniqueMapped = mapper.apply(uniqueResult);
			if (!Objects.equals(uniqueMapped, onlyElement)) {
				failWithMessage("Expected query unique to be equal to <%s>, but was <%s>", onlyElement, uniqueResult);
			}
		}
		catch (Exception e) { // SUPPRESS IllegalCatch
			failWithMessage("Expected query unique not to throw", e);
		}
		return myself;
	}

	AbstractQueryAssert<ELEMENT, MAPPED> doesNotHaveDuplicates() {
		iterables.assertDoesNotHaveDuplicates(info, actual);
		return myself;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	final AbstractQueryAssert<ELEMENT, MAPPED> contains(MAPPED... elements) {
		checkArgument(elements.length > 0, "use isEmpty instead"); // SUPPRESS MultipleStringLiterals
		List<? extends MAPPED> mapped = convertElements();
		iterables.assertContains(info, mapped, elements);
		checkContains(elements);
		return myself;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	final AbstractQueryAssert<ELEMENT, MAPPED> containsExactly(MAPPED... elements) {
		checkArgument(elements.length > 0, "use isEmpty instead"); // SUPPRESS MultipleStringLiterals
		List<? extends MAPPED> mapped = convertElements();
		iterables.assertContainsExactlyInAnyOrder(info, mapped, elements);
		checkContains(elements);
		return myself;
	}

	AbstractQueryAssert<ELEMENT, MAPPED> doesNotContain(Object... elements) {
		iterables.assertDoesNotContain(info, actual, elements);
		for (Object element : elements) {
			if (actual.contains(element)) {
				failWithMessage("Expected query to not contain <%s> but did", element);
			}
		}
		return myself;
	}

	<X> AbstractQueryAssert<ELEMENT, X> extracting(Function<? super MAPPED, ? extends X> nextMapper) {
		Function<ELEMENT, ? extends X> newMapper = mapper.andThen(nextMapper);
		return new AbstractQueryAssert<>(actual, newMapper, filter);
	}

	AbstractQueryAssert<ELEMENT, MAPPED> filteredOn(Predicate<? super ELEMENT> nextFilter) {
		@SuppressWarnings("unchecked")
		Predicate<? super ELEMENT> newFilter = ((Predicate<ELEMENT>) filter).and(nextFilter);
		return new AbstractQueryAssert<>(actual, mapper, newFilter);
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

	private void checkUniqueThrows(Class<? extends Throwable> exceptionClass) {
		try {
			ELEMENT uniqueResult = this.actual.unique();
			MAPPED uniqueMapped = mapper.apply(uniqueResult);
			failWithMessage("Expected query unique to throw, but it returned <%s>", uniqueMapped);
		}
		catch (Exception e) { // SUPPRESS IllegalCatch
			if (!exceptionClass.isInstance(e)) {
				failWithMessage("Expected query unique to throw " + exceptionClass.getSimpleName(), e);
			}
		}
	}

	@SafeVarargs
	private final void checkContains(MAPPED... elements) {
		if (elements.length > 2) { // SUPPRESS AvoidLiteralsInIfCondition
			checkUniqueThrows(IllegalArgumentException.class);
		}
		for (MAPPED element : elements) {
			if (!actual.contains(element)) {
				failWithMessage("Expected query to contain <%s> but didn't", element);
			}
		}
	}

	private List<? extends MAPPED> convertElements() {
		return actual.stream().filter(filter).map(mapper).collect(toList());
	}
}
