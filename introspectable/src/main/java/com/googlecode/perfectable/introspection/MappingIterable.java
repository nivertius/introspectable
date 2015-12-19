package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public abstract class MappingIterable<T> implements Iterable<T> {

	@SafeVarargs
	static <T> MappingIterable<T> create(Function<T, Stream<T>> mapper, T... seeds) {
		return new MappingIterable<T>() {

			@Override
			protected Collection<T> seed() {
				return ImmutableList.copyOf(seeds);
			}

			@Override
			protected Collection<T> map(T current) {
				return mapper.apply(current).collect(Collectors.toList());
			}

		};
	}

	protected abstract Collection<T> seed();

	protected abstract Collection<T> map(T current);

	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public final Iterator<T> iterator() {
		return new MappingIterator();
	}

	private final class MappingIterator implements Iterator<T> {
		private final Deque<T> left = new LinkedList<>(seed());
		
		@Override
		public boolean hasNext() {
			return !this.left.isEmpty();
		}
		
		@Override
		public T next() {
			checkState(hasNext());
			T current = this.left.pop();
			Collection<T> generated = map(current);
			this.left.addAll(generated);
			return current;
		}
	}

}
