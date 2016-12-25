package org.perfectable.introspection.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.Introspections.introspect;

public interface Property<CT, PT> {

	@Nullable
	PT get(CT bean);

	void set(CT bean, @Nullable PT value);

	String name();

	Class<PT> type();

	boolean isReadable();

	boolean isWriteable();

	default BoundProperty<CT, PT> bind(CT bean) {
		return BoundProperty.of(bean, this);
	}

	static <CX> Property<CX, Object> raw(Class<CX> beanClass, String name) {
		return from(beanClass, name, Object.class);
	}

	static <CX, PX> Property<CX, PX> from(Class<CX> beanClass, String name, Class<PX> type) {
		checkArgument(beanClass != null);
		Optional<Field> field = introspect(beanClass).fields().named(name).typed(type).option();
		if (field.isPresent()) {
			return FieldProperty.fromField(field.get());
		}
		Optional<Method> getter = ReadOnlyMethodProperty.findGetter(beanClass, name, type);
		Optional<Method> setter = WriteOnlyMethodProperty.findSetter(beanClass, name, type);
		if (setter.isPresent() && getter.isPresent()) {
			return ReadWriteMethodProperty.forGetterSetter(getter.get(), setter.get());
		}
		if (getter.isPresent()) {
			return ReadOnlyMethodProperty.forGetter(getter.get());
		}
		if (setter.isPresent()) {
			return WriteOnlyMethodProperty.forSetter(setter.get());
		}
		throw new IllegalArgumentException("No property " + name + " for " + beanClass);
	}

	static Property<?, ?> fromField(Field field) {
		checkNotNull(field);
		return FieldProperty.fromField(field);
	}

	static <CX> Property<CX, ?> fromField(Class<CX> beanClass, Field field) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(beanClass));
		return FieldProperty.fromField(field);
	}

	static <CX, PX> Property<CX, PX> fromField(Class<CX> beanClass, Field field, Class<PX> type) {
		checkNotNull(field);
		checkArgument(field.getDeclaringClass().isAssignableFrom(beanClass));
		checkArgument(field.getType().equals(type));
		checkArgument(!Modifier.isStatic(field.getModifiers()));
		return FieldProperty.fromField(field);
	}

	static Property<?, ?> fromSetter(Method setter) {
		return WriteOnlyMethodProperty.forSetter(setter);
	}

}
