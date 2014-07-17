package com.googlecode.perfectable.introspection;

import java.util.Iterator;

import com.google.common.collect.Iterators;

public final class InterfaceQuery<X> implements Iterable<Class<? super X>> {
	public static <X> InterfaceQuery<X> of(Class<X> type) {
		return new InterfaceQuery<>(type);
	}

	private final InheritanceChain<X> chain;
	
	private InterfaceQuery(Class<X> type) {
		this.chain = InheritanceChain.startingAt(type);
	}
	
	@Override
	public Iterator<Class<? super X>> iterator() {
		return new AbstractClassElementsIterator<X, Class<? super X>>(this.chain) {

			@Override
			protected Iterator<Class<? super X>> extractElements(Class<?> nextClass) {
				@SuppressWarnings("unchecked")
				final Class<? super X>[] interfaces = (Class<? super X>[]) nextClass.getInterfaces();
				return Iterators.forArray(interfaces);
			}
			
		};
	}
}
