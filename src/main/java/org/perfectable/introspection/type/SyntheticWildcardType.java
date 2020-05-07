package org.perfectable.introspection.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

final class SyntheticWildcardType implements WildcardType {
	private static final Collector<CharSequence, ?, String> BOUND_JOINER = Collectors.joining(" & ");

	SyntheticWildcardType(Type[] lowerBounds, Type[] upperBounds) {
		this.lowerBounds = lowerBounds.clone();
		this.upperBounds = upperBounds.clone();
	}

	private final Type[] lowerBounds;
	private final Type[] upperBounds;

	@Override
	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	@Override
	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	@Override
	public String getTypeName() {
		StringBuilder builder = new StringBuilder("?");
		if (lowerBounds.length > 0) {
			String lowerBoundsString =
				Stream.of(lowerBounds).map(Type::getTypeName).collect(BOUND_JOINER);
			builder.append(" super ").append(lowerBoundsString);
		}
		if (upperBounds.length > 0) {
			String upperBoundsString =
				Stream.of(upperBounds).map(Type::getTypeName).collect(BOUND_JOINER);
			builder.append(" extends ").append(upperBoundsString);
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof WildcardType)) {
			return false;
		}
		WildcardType other = (WildcardType) obj;
		return Arrays.equals(lowerBounds, other.getLowerBounds())
			&& Arrays.equals(upperBounds, other.getUpperBounds());
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(lowerBounds), Arrays.hashCode(upperBounds));
	}
}
