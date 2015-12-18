package com.googlecode.perfectable.introspection;

import java.util.Arrays;
import java.util.Collection;

import com.googlecode.perfectable.introspection.bean.PropertySlot;

public abstract class Injection<T> {
	public static <TX, PX> Injection<TX> create(PropertySlot<TX, PX> slot, PX value) {
		return new PropertyInjection<>(slot, value);
	}

	public static <TX, PX> Injection<TX> create(Class<TX> beanType, String propertyName, Class<PX> propertyType,
			PX value) {
		PropertySlot<TX, PX> slot = PropertySlot.from(beanType, propertyName, propertyType);
		return new PropertyInjection<>(slot, value);
	}

	@SafeVarargs
	public static <TX> CompositeInjection<TX> createComposite(Injection<TX>... injections) {
		return new CompositeInjection<>(injections);
	}

	public abstract void perform(T target);

	private static class PropertyInjection<T, X> extends Injection<T> {
		private final PropertySlot<T, X> slot;
		private final X value;

		public PropertyInjection(PropertySlot<T, X> slot, X value) {
			this.slot = slot;
			this.value = value;
		}

		@Override
		public void perform(T target) {
			this.slot.put(target).set(this.value);
		}
	}

	public static class CompositeInjection<T> extends Injection<T> {
		private final Collection<Injection<T>> components;

		@SafeVarargs
		protected CompositeInjection(Injection<T>... injections) {
			this.components = Arrays.asList(injections);
		}

		@Override
		public void perform(T target) {
			for(Injection<T> component : this.components) {
				component.perform(target);
			}
		}

		public void add(Injection<T> component) {
			this.components.add(component);
		}
	}

}
