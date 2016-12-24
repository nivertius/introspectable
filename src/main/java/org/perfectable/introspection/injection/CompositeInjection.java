package org.perfectable.introspection.injection;

import com.google.common.collect.ImmutableList;

public final class CompositeInjection<T> implements Injection<T> {
	private final ImmutableList<? extends Injection<? super T>> components;

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <TX> CompositeInjection<TX> create(Injection<? super TX>... injections) {
		return new CompositeInjection<TX>(ImmutableList.copyOf(injections));
	}

	private CompositeInjection(ImmutableList<? extends Injection<? super T>> injections) {
		this.components = injections;
	}

	@Override
	public void perform(T target) {
		for (Injection<? super T> component : this.components) {
			component.perform(target);
		}
	}
}
