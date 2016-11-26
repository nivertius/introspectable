package org.perfectable.introspection.bean;

import org.perfectable.introspection.Methods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class WriteOnlyMethodProperty<CT, PT> implements Property<CT, PT> {
	private final Method setter;

	public static <CX, PX> WriteOnlyMethodProperty<CX, PX> forSetter(Method setter) {
		checkNotNull(setter);
		checkArgument(Methods.isSetter(setter));
		return new WriteOnlyMethodProperty<>(setter);
	}

	private WriteOnlyMethodProperty(Method setter) {
		this.setter = checkNotNull(setter);
	}

	// checked at construction
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public PT get(CT bean) {
		throw new IllegalStateException("Property is not readable");
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
		return Methods.propertyNameFromSetter(this.setter);
	}

	@Override
	public Class<PT> type() {
		Class<?>[] parameterTypes = this.setter.getParameterTypes();
		@SuppressWarnings("unchecked") // checked at construction
		Class<PT> firstParameterType = (Class<PT>) parameterTypes[0];
		return checkNotNull(firstParameterType);
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWriteable() {
		return Methods.isCallable(this.setter);
	}
}
