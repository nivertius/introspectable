package com.googlecode.perfectable.introspection;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

abstract class AbstractClassElementsIterator<X, E> extends AbstractIterator<E> {
	Iterator<Class<? super X>> chainIterator;
	Iterator<E> current = Iterators.emptyIterator();
	
	public AbstractClassElementsIterator(InheritanceChain<X> chain) {
		this.chainIterator = chain.iterator();
	}

	@Override
	protected final E computeNext() {
		while(!this.current.hasNext()) {
			if(!this.chainIterator.hasNext()) {
				return endOfData();
			}
			final Class<?> nextClass = this.chainIterator.next();
			this.current = extractElements(nextClass);
		}
		return this.current.next();
	}
	
	protected abstract Iterator<E> extractElements(final Class<?> nextClass);
}
