package org.perfectable.introspection.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.perfectable.introspection.bean.Property;

public abstract class Injection<T> {
	
	static <TX> Injection<TX> create(Field field, Object value) {
		@SuppressWarnings("unchecked")
		Property<TX, Object> property = (Property<TX, Object>) Property.fromField(field);
		return create(property, value);
	}
	
	static <TX> Injection<TX> create(Method setter, Object value) {
		@SuppressWarnings("unchecked")
		Property<TX, Object> property = (Property<TX, Object>) Property.fromSetter(setter);
		return create(property, value);
	}
	
	public static <TX, PX> Injection<TX> create(Property<TX, PX> property, PX value) {
		return new PropertyInjection<>(property, value);
	}
	
	public static <TX, PX> Injection<TX> create(Class<TX> beanType, String propertyName, Class<PX> propertyType,
			PX value) {
		Property<TX, PX> property = Property.from(beanType, propertyName, propertyType);
		return new PropertyInjection<>(property, value);
	}
	
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <TX> CompositeInjection<TX> createComposite(Injection<TX>... injections) {
		return new CompositeInjection<>(injections);
	}
	
	public abstract void perform(T target);
	
	private static class PropertyInjection<T, X> extends Injection<T> {
		private final Property<T, X> property;
		private final X value;
		
		public PropertyInjection(Property<T, X> property, X value) {
			this.property = property;
			this.value = value;
		}
		
		@Override
		public void perform(T target) {
			this.property.bind(target).set(this.value);
		}
	}
	
	public static class CompositeInjection<T> extends Injection<T> {
		private final Collection<Injection<T>> components;
		
		@SafeVarargs
		@SuppressWarnings("varargs")
		protected CompositeInjection(Injection<T>... injections) {
			this.components = Lists.newArrayList(injections);
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
