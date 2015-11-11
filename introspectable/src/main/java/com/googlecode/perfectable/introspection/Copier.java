package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.googlecode.perfectable.introspection.bean.Bean;

public final class Copier<T> implements UnaryOperator<T> {
	private final Class<T> beanClass;

	public static final <X> Copier<X> forClass(Class<X> beanClass) {
		return new Copier<>(beanClass);
	}

	public static final <X> X copy(X source) {
		@SuppressWarnings("unchecked")
		Class<X> sourceClass = checkNotNull((Class<X>) source.getClass());
		return Copier.<X> forClass(sourceClass).perform(source);
	}

	private Copier(Class<T> beanClass) {
		this.beanClass = checkNotNull(beanClass);
	}

	@SuppressWarnings("null")
	@Override
	public @Nullable T apply(@Nullable T input) {
		if(input == null) {
			return null;
		}
		return this.perform(input);
	}

	private T perform(T input) {
		checkNotNull(input);
		@SuppressWarnings("unchecked")
		final Class<? extends T> inputClass = (Class<? extends T>) input.getClass();
		T instance = Classes.instantiate(inputClass);
		Bean<T> inputBean = Bean.from(input);
		inputBean.fieldProperties()
				.forEach(property -> property.copy(instance));
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
