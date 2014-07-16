package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;

import javax.annotation.Nullable;

public class InheritanceChain<T> implements Iterable<Class<? super T>> {

	private final Class<T> startClass;
	private final @Nullable Class<? super T> stopClass;

	public static <T> InheritanceChain<T> startingAt(Class<T> startClass) {
		checkNotNull(startClass);
		return new InheritanceChain<>(startClass, null);
	}

	public InheritanceChain<T> upToExcluding(@SuppressWarnings("hiding") Class<? super T> stopClass) {
		checkNotNull(stopClass);
		return new InheritanceChain<>(this.startClass, stopClass);
	}

	public InheritanceChain<T> upToIncluding(@SuppressWarnings("hiding") Class<? super T> stopClass) {
		checkNotNull(stopClass);
		return new InheritanceChain<>(this.startClass, stopClass.getSuperclass());
	}

	private InheritanceChain(Class<T> startClass, Class<? super T> stopClass) {
		this.startClass = startClass;
		this.stopClass = stopClass;
	}

	@Override
	public Iterator<Class<? super T>> iterator() {
		return new InheritanceIterator();
	}

	protected class InheritanceIterator implements Iterator<Class<? super T>> {
		private @Nullable Class<? super T> currentClass = InheritanceChain.this.startClass;

		@Override
		public boolean hasNext() {
			return this.currentClass != InheritanceChain.this.stopClass;
		}

		@Override
		public Class<? super T> next() {
			checkState(hasNext());
			checkState(this.currentClass != null);
			final Class<? super T> result = this.currentClass;
			this.currentClass = result.getSuperclass();
			return result;
		}

	}
}
