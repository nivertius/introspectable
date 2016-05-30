package org.perfectable.introspection.bean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Nullable;

import org.perfectable.introspection.Fields;
import org.perfectable.introspection.Introspection;
import org.perfectable.introspection.Methods;

import com.google.common.base.Throwables;

public abstract class Property<CT, PT> {
	
	public abstract boolean isReadable();
	
	public abstract boolean isWriteable();
	
	@Nullable
	public abstract PT get(CT bean);
	
	public abstract void set(CT bean, @Nullable PT value);
	
	public abstract String name();
	
	public abstract Class<PT> type();
	
	public BoundProperty<CT, PT> bind(CT bean) {
		return new BoundProperty<>(this, bean);
	}
	
	public static <CX> Property<CX, Object> raw(Class<CX> beanClass, String name) {
		return from(beanClass, name, Object.class);
	}
	
	public static <CX, PX> Property<CX, PX> from(Class<CX> beanClass, String name, Class<PX> type) {
		if(beanClass == null) {
			throw new IllegalArgumentException();
		}
		Optional<Field> field = Introspection.of(beanClass).fields().named(name).typed(type).option();
		if(field.isPresent()) {
			return new FieldProperty<>(field.get());
		}
		Optional<Method> getter = Methods.findGetter(beanClass, name, type);
		Optional<Method> setter = Methods.findSetter(beanClass, name, type);
		if(setter.isPresent() || getter.isPresent()) {
			return new MethodProperty<>(getter, setter);
		}
		throw new IllegalArgumentException("No property " + name + " for " + beanClass);
	}
	
	public static Property<?, ?> fromField(Field field) {
		checkNotNull(field);
		return new FieldProperty<>(field);
	}
	
	public static <CX> Property<CX, ?> fromField(Class<CX> beanClass, Field field) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(beanClass));
		return new FieldProperty<>(field);
	}
	
	public static <CX, PX> Property<CX, PX> fromField(Class<CX> beanClass, Field field, Class<PX> type) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(beanClass));
		checkArgument(field.getType().equals(type));
		checkArgument(!Fields.isStatic(field));
		return new FieldProperty<>(field);
	}
	
	public static Property<?, ?> fromSetter(Method setter) {
		checkNotNull(setter);
		checkArgument(Methods.isSetter(setter));
		return new MethodProperty<>(Optional.empty(), Optional.of(setter));
	}
	
	private static class FieldProperty<CT, PT> extends Property<CT, PT> {
		private final Field field;
		
		public FieldProperty(Field field) {
			this.field = field;
			this.field.setAccessible(true);
		}
		
		@Override
		public void set(CT bean, @Nullable PT value) {
			try {
				this.field.set(bean, value);
			}
			catch(IllegalArgumentException | IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}
		
		// checked at construction
		@Override
		@Nullable
		@SuppressWarnings("unchecked")
		public PT get(CT bean) {
			try {
				return (PT) this.field.get(bean);
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
		
		public MethodProperty(Optional<Method> getter, Optional<Method> setter) {
			checkArgument(getter.isPresent() || setter.isPresent());
			this.getter = getter;
			this.setter = setter;
		}
		
		// checked at construction
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public PT get(CT bean) {
			checkState(this.getter.isPresent());
			try {
				return (PT) this.getter.get().invoke(bean);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public void set(CT bean, @Nullable PT value) {
			checkState(this.setter.isPresent());
			try {
				this.setter.get().invoke(bean, value);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw Throwables.propagate(e);
			}
		}
		
		@Override
		public String name() {
			String unformatted = this.getter.orElseGet(this.setter::get).getName();
			// MARK boolean getter
			return String.valueOf(unformatted.charAt(3)).toLowerCase() + unformatted.substring(4);
		}
		
		@Override
		public Class<PT> type() {
			if(this.getter.isPresent()) {
				Method getterMethod = this.getter.get();
				@SuppressWarnings("unchecked")
				Class<PT> resultType = (Class<PT>) getterMethod.getReturnType();
				return checkNotNull(resultType);
			}
			else if(this.setter.isPresent()) {
				Method setterMethod = this.setter.get();
				Class<?>[] parameterTypes = setterMethod.getParameterTypes();
				@SuppressWarnings("unchecked")
				// checked at construction
				Class<PT> firstParameterType = (Class<PT>) parameterTypes[0];
				return checkNotNull(firstParameterType);
			}
			else {
				throw new AssertionError("MethodProperty created without setter or getter, which is forbidden by constructor");
			}
		}
		
		@Override
		public boolean isReadable() {
			if(!this.getter.isPresent()) {
				return false;
			}
			Method getterMethod = this.getter.get();
			return Methods.isCallable(getterMethod);
		}
		
		@Override
		public boolean isWriteable() {
			if(!this.setter.isPresent()) {
				return false;
			}
			Method setterMethod = this.setter.get();
			return Methods.isCallable(setterMethod);
		}
		
	}
	
}
