package org.perfectable.introspection.bean;

import com.google.common.base.Throwables;
import org.perfectable.introspection.Methods;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

final class ReadOnlyMethodProperty<CT, PT> implements Property<CT, PT> {
	private final Method getter;

	public static <CX, PX> ReadOnlyMethodProperty<CX, PX> forGetter(Method getter) {
		return new ReadOnlyMethodProperty<>(getter);
	}

	private ReadOnlyMethodProperty(Method getter) {
		this.getter = checkNotNull(getter);
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
		throw new IllegalStateException("Property is not writeable");
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
		return false;
	}
}
