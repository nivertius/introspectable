package org.perfectable.introspection.bean;

import com.google.common.base.Throwables;
import org.perfectable.introspection.Methods;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

final class ReadWriteMethodProperty<CT, PT> implements Property<CT, PT> {
	private final Method getter;
	private final Method setter;

	public static <CX, PX> ReadWriteMethodProperty<CX, PX> forGetterSetter(Method getter, Method setter) {
		checkNotNull(getter);
		checkNotNull(setter);
		// MARK check getter and setter compatibility
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
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void set(CT bean, @Nullable PT value) {
		try {
			this.setter.invoke(bean, value);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String name() {
		String unformatted = this.getter.getName();
		// MARK boolean getter
		return String.valueOf(unformatted.charAt(3)).toLowerCase() + unformatted.substring(4);
	}

	@Override
	public Class<PT> type() {
		@SuppressWarnings("unchecked")
		Class<PT> resultType = (Class<PT>) this.getter.getReturnType();
		return checkNotNull(resultType);
	}

	@Override
	public boolean isReadable() {
		return Methods.isCallable(getter);
	}

	@Override
	public boolean isWriteable() {
		return Methods.isCallable(setter);
	}

}
