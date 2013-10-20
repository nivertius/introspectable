package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

public abstract class Property<CT, PT> {

	protected final Object bean;

	protected Property(CT bean) {
		this.bean = bean;
	}

	@Nullable
	public abstract PT get();

	public abstract void set(@Nullable PT value);

	public final Optional<PT> optional() {
		return Optional.fromNullable(this.get());
	}

	public abstract String name();

	public abstract Class<PT> type();

	public final void copy(CT other) {
		PT current = this.get();
		this.slot().put(other).set(current);
	}

	public final PropertySlot<CT, PT> slot() {
		return PropertySlot.from((Class<CT>) this.bean.getClass(), this.name(), this.type());
	}

	public static <CX> Property<CX, Object> raw(CX bean, String name) {
		return from(bean, name, Object.class);
	}

	public static <CX, PX> Property<CX, PX> from(CX bean, String name, Class<PX> type) {
		Class<?> beanClass = bean.getClass();
		if(beanClass == null) {
			throw new IllegalArgumentException();
		}
		Field field = Fields.find(beanClass, name, type);
		if(field != null) {
			return new FieldProperty<>(bean, field);
		}
		Optional<Method> getter = Methods.findGetter(beanClass, name, type);
		Optional<Method> setter = Methods.findSetter(beanClass, name, type);
		return new MethodProperty<>(bean, getter, setter);
	}

	public static <CX> Property<CX, Object> from(CX bean, Field field) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(bean.getClass()));
		return new FieldProperty<>(bean, field);
	}

	public static <CX, PX> Property<CX, PX> from(CX bean, Field field, Class<PX> type) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(bean.getClass()));
		checkArgument(field.getType().equals(type));
		return new FieldProperty<>(bean, field);
	}

	private static class FieldProperty<CT, PT> extends Property<CT, PT> {
		private Field field;

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
				throw new RuntimeException(e); // TODO Auto-generated catch block
			}
		}

		@Override
		@Nullable
		public PT get() {
			try {
				return (PT) this.field.get(this.bean);
			}
			catch(IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e); // TODO Auto-generated catch block
			}
		}

		@SuppressWarnings("null")
		@Override
		public String name() {
			return this.field.getName();
		}

		@SuppressWarnings("null")
		@Override
		public Class<PT> type() {
			return (Class<PT>) this.field.getType();
		}
	}

	private static class MethodProperty<CT, PT> extends Property<CT, PT> {
		private final Optional<Method> getter;
		private final Optional<Method> setter;

		public MethodProperty(CT bean, Optional<Method> getter, Optional<Method> setter) {
			super(bean);
			checkArgument(!getter.isPresent() || getter.get().getDeclaringClass().equals(bean.getClass()));
			checkArgument(!setter.isPresent() || setter.get().getDeclaringClass().equals(bean.getClass()));
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		@Nullable
		public PT get() {
			checkState(this.getter.isPresent());
			try {
				return (PT) this.getter.get().invoke(this.bean);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e); // TODO Auto-generated catch block
			}
		}

		@Override
		public void set(@Nullable PT value) {
			checkState(this.setter.isPresent());
			try {
				this.setter.get().invoke(this.bean, value);
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e); // TODO Auto-generated catch block
			}
		}

		@Override
		public String name() {
			String unformatted = this.getter.or(this.setter).get().getName();
			return String.valueOf(unformatted.charAt(3)).toLowerCase() + unformatted.substring(4);
		}

		@Override
		public Class<PT> type() {
			if(this.getter.isPresent()) {
				final Method getterMethod = this.getter.get();
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
				throw new RuntimeException(); // MARK
			}
		}

	}

}
