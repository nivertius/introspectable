package org.perfectable.introspection.query;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;

public abstract class AbstractQuery<E, Q extends AbstractQuery<E, ? extends Q>> implements Iterable<E> {

	public abstract Q filter(Predicate<? super E> filter);

	public abstract Stream<E> stream();

	@Override
	public Iterator<E> iterator() {
		return stream().iterator();
	}

	public final E unique() {
		return Iterators.getOnlyElement(iterator());
	}

	public boolean isPresent() {
		return iterator().hasNext();
	}

	public final Optional<E> option() {
		Iterator<E> iterator = iterator();
		if (iterator.hasNext()) {
			return Optional.of(iterator.next());
		}
		return Optional.empty();
	}

	AbstractQuery() {
		// package extension only
	}
}
