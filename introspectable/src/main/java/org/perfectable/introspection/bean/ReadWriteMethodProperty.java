package org.perfectable.introspection.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.Introspections.introspect;
import static org.perfectable.introspection.bean.ReadOnlyMethodProperty.propertyNameFromGetter;

final class ReadWriteMethodProperty<CT, PT> implements Property<CT, PT> {
	private final Method getter;
	private final Method setter;

	public static <CX, PX> ReadWriteMethodProperty<CX, PX> forGetterSetter(Method getter, Method setter) {
		checkNotNull(getter);
		checkNotNull(setter);
		// TODO check getter and setter compatibility
		return new ReadWriteMethodProperty<>(getter, setter);
	}

	private ReadWriteMethodProperty(Method getter, Method setter) {
		this.getter = getter;
		this.setter = setter;
	}

	// checked at construction
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public PT get(CT bean) {
		try {
			return (PT) this.getter.invoke(bean);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
		}
	}

	@Override
	public void set(CT bean, @Nullable PT value) {
		try {
			this.setter.invoke(bean, value);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
		}
	}

	@Override
	public String name() {
		return propertyNameFromGetter(this.getter);
	}

	@Override
	public Class<PT> type() {
		@SuppressWarnings("unchecked")
		Class<PT> resultType = (Class<PT>) this.getter.getReturnType();
		return checkNotNull(resultType);
	}

	@Override
	public boolean isReadable() {
		return introspect(getter).isCallable();
	}

	@Override
	public boolean isWriteable() {
		return introspect(setter).isCallable();
	}

}
