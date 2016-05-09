package org.perfectable.introspection.query;

import java.util.Collection;

import org.perfectable.introspection.MappingIterable;

import com.google.common.collect.ImmutableList;

public final class InterfaceQuery<X> extends MappingIterable<Class<? super X>> implements Iterable<Class<? super X>> {
	public static <X> InterfaceQuery<X> of(Class<X> type) {
		ImmutableList<Class<? super X>> initial;
		if(type.isInterface()) {
			initial = ImmutableList.of(type);
		}
		else {
			initial = ImmutableList.copyOf(safeGetInterfaces(type));
		}
		return new InterfaceQuery<>(initial);
	}
	
	private final ImmutableList<Class<? super X>> initial;
	
	public InterfaceQuery(ImmutableList<Class<? super X>> initial) {
		this.initial = initial;
	}
	
	@Override
	protected Collection<Class<? super X>> seed() {
		return this.initial;
	}
	
	@Override
	protected Collection<Class<? super X>> map(Class<? super X> current) {
		@SuppressWarnings("unchecked")
		Class<X> castedCurrent = (Class<X>) current;
		return safeGetInterfaces(castedCurrent);
	}
	
	private static <X> Collection<Class<? super X>> safeGetInterfaces(Class<X> type) {
		@SuppressWarnings("unchecked")
		Class<? super X>[] interfaceArray = (Class<? super X>[]) type.getInterfaces();
		return ImmutableList.copyOf(interfaceArray);
	}
	
}
