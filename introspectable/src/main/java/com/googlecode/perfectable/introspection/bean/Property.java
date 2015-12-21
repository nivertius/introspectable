package com.googlecode.perfectable.introspection.bean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import com.googlecode.perfectable.introspection.Fields;
import com.googlecode.perfectable.introspection.Introspection;
import com.googlecode.perfectable.introspection.Methods;

public abstract class Property<CT, PT> {
	
	protected final CT bean;
	
	protected Property(CT bean) {
		this.bean = bean;
	}
	
	public abstract boolean isReadable();
	
	public abstract boolean isWriteable();
	
	@Nullable
	public abstract PT get();
	
	public abstract void set(@Nullable PT value);
	
	public final Optional<PT> optional() {
		return Optional.ofNullable(this.get());
	}
	
	public abstract String name();
	
	public abstract Class<PT> type();
	
	public final void copy(CT other) {
		PT current = this.get();
		this.slot().put(other).set(current);
	}
	
	public final PropertySlot<CT, PT> slot() {
		@SuppressWarnings("unchecked")
		final Class<? extends CT> beanClass = (Class<? extends CT>) this.bean.getClass();
		return PropertySlot.from(beanClass, this.name(), this.type());
	}
	
	public static <CX> Property<CX, Object> raw(CX bean, String name) {
		return from(bean, name, Object.class);
	}
	
	public static <CX, PX> Property<CX, PX> from(CX bean, String name, Class<PX> type) {
		Class<?> beanClass = bean.getClass();
		if(beanClass == null) {
			throw new IllegalArgumentException();
		}
		Optional<Field> field = Introspection.of(beanClass).fields().named(name).typed(type).option();
		if(field.isPresent()) {
			return new FieldProperty<>(bean, field.get());
		}
		Optional<Method> getter = Methods.findGetter(beanClass, name, type);
		Optional<Method> setter = Methods.findSetter(beanClass, name, type);
		if(setter.isPresent() || getter.isPresent()) {
			return new MethodProperty<>(bean, getter, setter);
		}
		throw new IllegalArgumentException("No property " + name + " for " + beanClass);
	}
	
	public static <CX> Property<CX, ?> from(CX bean, Field field) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(bean.getClass()));
		return new FieldProperty<>(bean, field);
	}
	
	public static <CX, PX> Property<CX, PX> from(CX bean, Field field, Class<PX> type) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(bean.getClass()));
		checkArgument(field.getType().equals(type));
		checkArgument(!Fields.isStatic(field));
		return new FieldProperty<>(bean, field);
	}
	
	private static class FieldProperty<CT, PT> extends Property<CT, PT> {
		private final Field field;
		
		public FieldProperty(CT bean, Field field) {
			super(bean);
			this.field = field;
			this.field.setAccessible(true);
		}
		
		@Override
		public void set(@Nullable PT value) {
			try {
				this.field.set(this.bean, value);
			}
			catch(IllegalArgumentException | IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}
		
		// checked at construction
		@Override
		@Nullable
		@SuppressWarnings("unchecked")
		public PT get() {
			try {
				return (PT) this.field.get(this.bean);
			}
			catch(IllegalArgumentException | IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public String name() {
			return this.field.getName();
		}
		
		// checked at construction
		@Override
		@SuppressWarnings("unchecked")
		public Class<PT> type() {
			return (Class<PT>) this.field.getType();
		}
		
		@Override
		public boolean isReadable() {
			return Fields.isGettable(this.field);
		}
		
		@Override
		public boolean isWriteable() {
			return Fields.isSettable(this.field);
		}
	}
	
	private static class MethodProperty<CT, PT> extends Property<CT, PT> {
		private final Optional<Method> getter;
		private final Optional<Method> setter;
		
		public MethodProperty(CT bean, Optional<Method> getter, Optional<Method> setter) {
			super(bean);
			checkArgument(getter.isPresent() || setter.isPresent());
			checkArgument(!getter.isPresent() ||
					Methods.isGetter(getter.get()) && getter.get().getDeclaringClass().equals(bean.getClass()));
			checkArgument(!setter.isPresent() ||
					Methods.isSetter(setter.get()) && setter.get().getDeclaringClass().equals(bean.getClass()));
			this.getter = getter;
			this.setter = setter;
		}
		
		// checked at construction
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public PT get() {
			checkState(this.getter.isPresent());
			try {
				return (PT) this.getter.get().invoke(this.bean);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public void set(@Nullable PT value) {
			checkState(this.setter.isPresent());
			try {
				this.setter.get().invoke(this.bean, value);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public String name() {
			String unformatted = this.getter.orElseGet(this.setter::get).getName();
			return String.valueOf(unformatted.charAt(3)).toLowerCase() + unformatted.substring(4);
		}
		
		@Override
		public Class<PT> type() {
			if(this.getter.isPresent()) {
				final Method getterMethod = this.getter.get();
				@SuppressWarnings("unchecked")
				final Class<PT> resultType = (Class<PT>) getterMethod.getReturnType();
				return checkNotNull(resultType);
			}
			else if(this.setter.isPresent()) {
				final Method setterMethod = this.setter.get();
				final Class<?>[] parameterTypes = setterMethod.getParameterTypes();
				@SuppressWarnings("unchecked")
				// checked at construction
				final Class<PT> firstParameterType = (Class<PT>) parameterTypes[0];
				return checkNotNull(firstParameterType);
			}
			else {
				throw new RuntimeException("MethodProperty created without setter or getter, which is forbidden by constructor");
			}
		}
		
		@Override
		public boolean isReadable() {
			if(!this.getter.isPresent()) {
				return false;
			}
			final Method getterMethod = this.getter.get();
			return Methods.isCallable(getterMethod);
		}
		
		@Override
		public boolean isWriteable() {
			if(!this.setter.isPresent()) {
				return false;
			}
			final Method setterMethod = this.setter.get();
			return Methods.isCallable(setterMethod);
		}
		
	}
	
}
