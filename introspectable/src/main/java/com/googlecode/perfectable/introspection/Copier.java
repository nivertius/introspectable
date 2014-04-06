package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.Function;

public final class Copier<T> implements Function<T, T> {
	private final Class<T> beanClass;

	public static final <X> Copier<X> forClass(Class<X> beanClass) {
		return new Copier<>(beanClass);
	}

	public static final <X> X copy(X source) {
		@SuppressWarnings("null")
		Class<X> sourceClass = checkNotNull((Class<X>) source.getClass());
		return Copier.<X> forClass(sourceClass).perform(source);
	}

	private Copier(Class<T> beanClass) {
		this.beanClass = checkNotNull(beanClass);
	}

	@Override
	@Nullable
	public T apply(@Nullable T input) {
		if(input == null) {
			return null;
		}
		return this.perform(input);
	}

	private T perform(T input) {
		checkNotNull(input);
		T instance = Classes.instantiate((Class<T>) input.getClass());
		Bean<T> inputBean = Bean.from(input);
		for(Property<T, ?> property : inputBean.fieldProperties()) {
			property.copy(instance);
		}
		return instance;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof Copier)) {
			return false;
		}
		Copier<?> other = (Copier<?>) obj;
		return this.beanClass.equals(other.beanClass);
	}

	@Override
	public int hashCode() {
		return this.beanClass.hashCode();
	}
}
