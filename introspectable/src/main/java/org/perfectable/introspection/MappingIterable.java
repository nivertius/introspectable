package org.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
				@SuppressWarnings("null")
				Stream<T> stream = checkNotNull(mapper.apply(current));
				return stream.collect(Collectors.toList());
			}
			
		};
	}
	
	protected abstract Collection<T> seed();
	
	protected abstract Collection<T> map(T current);
	
	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new MappingIterator();
	}
	
	protected class MappingIterator implements Iterator<T> {
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
			push(generated);
			return current;
		}
		
		protected void push(Collection<T> generated) {
			this.left.addAll(generated);
		}
	}
	
	public abstract static class Unique<T> extends MappingIterable<T> {
		@Override
		public Iterator<T> iterator() {
			return new UniqueMappingIterator();
		}
		
		class UniqueMappingIterator extends MappingIterator {
			private final Set<T> processed = new HashSet<>(seed());
			
			@Override
			protected void push(Collection<T> generated) {
				super.push(Sets.difference(ImmutableSet.copyOf(generated), this.processed));
				this.processed.addAll(generated);
			}
		}
	}
	
}
