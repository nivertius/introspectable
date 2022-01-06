package org.perfectable.introspection.query;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CompatibleWith;
import kotlin.annotations.jvm.ReadOnly;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for all queries, which defines all convenient usage methods.
 *
 * @param <E> type of element this query will return/contain
 * @param <Q> type of queries returned by restrictions
 */
abstract class AbstractQuery<E extends @NonNull Object, Q extends AbstractQuery<E, ? extends Q>>
	implements Iterable<E> {

	/**
	 * Filter query elements by arbitrary predicate.
	 *
	 * <p>Immutability warning: queries are assumed as immutable, but this method allows providing any
	 * {@link Predicate}, which will be held in resulting query. Therefore, if stateful predicate is provided,
	 * immutability of resulting query will be compromised. However, this is rarely the case, because most of the
	 * filters provided will be non-capturing lambdas or static method references.
	 *
	 * @param filter predicate that will determine if this query will contain element
	 * @return query returning the same elements as this one, but only if they match the predicate
	 */
	public abstract Q filter(Predicate<? super E> filter);

	/**
	 * Checks if provided object is in elements that would be returned by this query.
	 *
	 * <p>This is often faster than checking by iteration, and most queries in library override this method,
	 * but this is not guaranteed, as it might not be possible.
	 *
	 * @param candidate object to check if it is contained in this query.
	 * @return if object would be returned by the query
	 */
	public boolean contains(@CompatibleWith("E") @Nullable Object candidate) {
		return Iterators.contains(iterator(), candidate);
	}

	/**
	 * Requests specific order of elements returned by this query.
	 *
	 * @param comparator how to compare elements of this query for requested order
	 * @return query returning the same elements as this one, but in specified order
	 */
	public abstract Q sorted(Comparator<? super E> comparator);

	/**
	 * Adapts this query to a Stream.
	 *
	 * @return stream with elements of this query
	 */
	public abstract Stream<E> stream();

	/**
	 * Returns single element that this query contains.
	 *
	 * @return single element matches by this query
	 * @throws NoSuchElementException if this query didn't contain any element
	 * @throws IllegalArgumentException if the query contains multiple elements
	 */
	public final E unique() {
		return Iterators.getOnlyElement(iterator());
	}

	/**
	 * Checks if any element was contained in query.
	 *
	 * @return true if at least one element is present in this query
	 */
	public boolean isPresent() {
		return iterator().hasNext();
	}

	/**
	 * Fetches only element present in this query, or empty optional.
	 *
	 * @return single element matched by this query, or empty optional.
	 * @throws IllegalArgumentException if the query contains multiple elements
	 */
	public final Optional<E> option() {
		Iterator<E> iterator = iterator();
		if (!iterator.hasNext()) {
			return Optional.empty();
		}
		E element = iterator.next();
		if (iterator.hasNext()) {
			throw new IllegalArgumentException("Multiple elements were present in the query");
		}
		return Optional.of(element);
	}

	@ReadOnly
	@Override
	public Iterator<E> iterator() {
		return stream().iterator();
	}

}
