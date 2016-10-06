package org.perfectable.introspection.bean;

import com.google.common.base.Throwables;
import org.perfectable.introspection.Fields;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

final class FieldProperty<CT, PT> implements Property<CT, PT> {
	private final Field field;

	public static <CX, PX> FieldProperty<CX, PX> fromField(Field field) {
		return new FieldProperty<>(field);
	}

	private FieldProperty(Field field) {
		this.field = field;
		this.field.setAccessible(true);
	}

	@Override
	public void set(CT bean, @Nullable PT value) {
		try {
			this.field.set(bean, value);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
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
		catch (IllegalArgumentException | IllegalAccessException e) {
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
