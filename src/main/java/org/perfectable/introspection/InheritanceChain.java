package org.perfectable.introspection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class InheritanceChain<T> implements Iterable<Class<? super T>> {

	private final Class<T> startClass;
	@Nullable
	private final Class<? super T> stopClass;

	public static <T> InheritanceChain<T> startingAt(Class<T> startClass) {
		checkNotNull(startClass);
		return new InheritanceChain<>(startClass, null);
	}

	public InheritanceChain<T> upToExcluding(@SuppressWarnings("hiding") Class<? super T> newStopClass) {
		checkNotNull(newStopClass);
		return new InheritanceChain<>(this.startClass, newStopClass);
	}

	public InheritanceChain<T> upToIncluding(@SuppressWarnings("hiding") Class<? super T> newStopClass) {
		checkNotNull(newStopClass);
		return new InheritanceChain<>(this.startClass, newStopClass.getSuperclass());
	}

	private InheritanceChain(Class<T> startClass, @Nullable Class<? super T> stopClass) {
		this.startClass = startClass;
		this.stopClass = stopClass;
	}

	public Stream<Class<? super T>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public Iterator<Class<? super T>> iterator() {
		return new InheritanceIterator();
	}

	protected class InheritanceIterator implements Iterator<Class<? super T>> {
		@Nullable
		private Class<? super T> currentClass = InheritanceChain.this.startClass;

		@Override
		public boolean hasNext() {
			return this.currentClass != InheritanceChain.this.stopClass;
		}

		@Override
		public Class<? super T> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			checkState(this.currentClass != null);
			Class<? super T> result = this.currentClass;
			this.currentClass = result.getSuperclass();
			return result;
		}

	}
}