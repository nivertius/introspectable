package org.perfectable.introspection.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

final class SyntheticParameterizedType implements ParameterizedType {
	private final Class<?> rawType;
	private final @Nullable Type ownerType;
	private final Type[] typeArguments;

	SyntheticParameterizedType(Class<?> rawType, @Nullable Type ownerType,
							   Type[] typeArguments) {
		checkArgument(typeArguments.length == rawType.getTypeParameters().length);
		this.rawType = rawType;
		this.ownerType = ownerType;
		this.typeArguments = typeArguments.clone();
	}

	@Override
	public Type[] getActualTypeArguments() {
		return typeArguments.clone();
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@SuppressWarnings("override.return.invalid")
	@Override
	public @Nullable Type getOwnerType() {
		return ownerType;
	}

	@Override
	public String getTypeName() {
		String argumentNames = Stream.of(typeArguments)
			.map(Type::getTypeName)
			.collect(Collectors.joining(",", "<", ">"));
		return rawType.getTypeName() + argumentNames;
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof ParameterizedType)) {
			return false;
		}
		ParameterizedType other = (ParameterizedType) obj;
		return rawType.equals(other.getRawType())
			&& Objects.equals(ownerType, other.getOwnerType())
			&& Arrays.equals(typeArguments, other.getActualTypeArguments());
	}

	@Override
	public int hashCode() {
		return Objects.hash(rawType, ownerType, Arrays.hashCode(typeArguments));
	}
}
