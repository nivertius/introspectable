package com.googlecode.perfectable.introspection;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;

public class InheritanceChain<T> implements Iterable<Class<? super T>> {

	private Class<T> startClass;

	public static <T> InheritanceChain<T> startingAt(Class<T> startClass) {
		return new InheritanceChain<>(startClass);
	}

	private InheritanceChain(Class<T> startClass) {
		this.startClass = startClass;
	}

	@Override
	public Iterator<Class<? super T>> iterator() {
		return new InheritanceIterator<>(this.startClass);
	}

	protected static class InheritanceIterator<X> extends AbstractIterator<Class<? super X>> {
		@Nullable
		private Class<? super X> currentClass;

		public InheritanceIterator(@Nullable Class<X> startClass) {
			this.currentClass = startClass;
		}

		@Override
		protected Class<? super X> computeNext() {
			final Class<? super X> result = this.currentClass;
			if(result == null) {
				return this.endOfData();
			}
			this.currentClass = result.getSuperclass();
			return result;
		}

	}
}
