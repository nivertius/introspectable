package com.googlecode.perfectable.introspection.query;

import java.util.Iterator;
import java.util.stream.Stream;

import com.googlecode.perfectable.introspection.InheritanceChain;

public final class InterfaceQuery<X> implements Iterable<Class<? super X>> {
	public static <X> InterfaceQuery<X> of(Class<X> type) {
		return new InterfaceQuery<>(type);
	}
	
	private final InheritanceChain<X> chain;

	private InterfaceQuery(Class<X> type) {
		this.chain = InheritanceChain.startingAt(type);
	}

	public Stream<Class<? super X>> stream() {
		return this.chain.stream()
				.flatMap(InterfaceQuery::safeGetInterfacesStream);
	}

	@SuppressWarnings("unchecked")
	private static <X> Stream<Class<? super X>> safeGetInterfacesStream(Class<? super X> candidateClass) {
		final Class<? super X>[] interfaceArray = (Class<? super X>[]) candidateClass.getInterfaces();
		return Stream.of(interfaceArray);
	}

	@Override
	public Iterator<Class<? super X>> iterator() {
		return stream().iterator();
	}
	
}
