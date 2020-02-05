package org.perfectable.introspection.type;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import kotlin.annotations.jvm.ReadOnly;

/**
 * {@link TypeView} that handles {@link WildcardType}.
 */
public final class WildcardTypeView extends AbstractTypeView<WildcardType> {
	WildcardTypeView(WildcardType type) {
		super(type);
	}

	/**
	 * Creates view over provided type.
	 *
	 * @param wildcard construct to wrap
	 * @return view wrapping specified type
	 */
	public static WildcardTypeView of(WildcardType wildcard) {
		return new WildcardTypeView(wildcard);
	}

	/**
	 * Produces view for unbounded wildcard.
	 *
	 * @return view of an unbounded wildcard type.
	 */
	public static WildcardTypeView unbounded() {
		return new WildcardTypeView(SyntheticWildcardType.UNBOUNDED);
	}

	/**
	 * Creates new builder for wildcards.
	 *
	 * @return fresh, unconfigured mutable builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Class<?> erasure() {
		Type[] bounds = type.getUpperBounds();
		if (bounds.length == 0) {
			return Object.class;
		}
		Type firstBound = bounds[0];
		return of(firstBound).erasure();
	}

	/**
	 * Extracts upper bounds for this wildcard.
	 *
	 * <p>In actual code, there can be only one upper bound, it is impossible to declare multiple bounds. But synthetic
	 * types can have multiple as reflection system supports it. Reflection system also supports type that has both
	 * upper and lower bounds, even in multiples.
	 *
	 * <p>If wildcard has no upper bound, this method will return singleton collection with {@link Object} class.
	 * This is following contract in {@link WildcardType#getUpperBounds}, which will return singleton array with
	 * {@link Object} class.
	 *
	 * @return upper bounds of this wildcard, wrapped in {@link TypeView}
	 */
	@ReadOnly
	public Collection<TypeView> upperBounds() {
		return upperBoundsStream().collect(Collectors.toList());
	}

	/**
	 * Extracts lower bounds for this wildcard.
	 *
	 * <p>In actual code, there can be only one lower bound, it is impossible to declare multiple bounds. But synthetic
	 * types can have multiple as reflection system supports it. Reflection system also supports type that has both
	 * upper and lower bounds, even in multiples.
	 *
	 * <p>If wildcard has no lower bound, this method will return empty collection. This is following contract
	 * in {@link WildcardType#getLowerBounds}, which will return empty array.
	 *
	 * @return lower bounds of this wildcard, wrapped in {@link TypeView}
	 */
	@ReadOnly
	public Collection<TypeView> lowerBounds() {
		return lowerBoundsStream().collect(Collectors.toList());
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to parameterized type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a wildcard type, and this call will fail
	 */
	@Deprecated
	@Override
	public ParameterizedTypeView asParameterized() throws IllegalStateException {
		throw new IllegalStateException("Wildcard type cannot be converted to parameterized view");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to class view.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a wildcard type, and this call will fail
	 */
	@Deprecated
	@Override
	public ClassView<?> asClass() throws IllegalStateException {
		throw new IllegalStateException("Wildcard type cannot be converted to class");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to type variable.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a wildcard type, and this call will fail
	 */
	@Deprecated
	@Override
	public TypeVariableView<?> asVariable() {
		throw new IllegalStateException("Wildcard type cannot be converted to type variable");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return this, as this is already variable view
	 * @deprecated the type system already knows this is a variable, and this call does not change the type
	 */
	@Deprecated
	@Override
	public WildcardTypeView asWildcard() {
		return this;
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to array type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a wildcard type, and this call will fail
	 */
	@Deprecated
	@Override
	public ArrayTypeView asArray() throws IllegalStateException {
		throw new IllegalStateException("Type variable cannot be converted to array type");
	}


	@Override
	public boolean isSubTypeOf(TypeView other) {
		return upperBoundsStream().anyMatch(bound -> bound.isSubTypeOf(other));
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitWildcard(this);
	}

	@Override
	public WildcardTypeView resolve(Type other) {
		return (WildcardTypeView) super.resolve(other);
	}

	@Override
	public WildcardTypeView resolve(TypeView other) {
		return (WildcardTypeView) super.resolve(other);
	}

	@Override
	WildcardTypeView declaredAs(TypeVariable<?> declaration) {
		Type[] declarationBounds = removeObjectClass(declaration.getBounds());
		Type[] wildcardBounds = removeObjectClass(type.getUpperBounds());
		List<Type> upperBounds = new ArrayList<>();
		for (Type declared : declarationBounds) {
			TypeView declaredView = of(declared);
			if (!(declared instanceof Class<?>)
					|| Stream.of(wildcardBounds)
						.filter(w -> !(w instanceof Class<?>))
						.noneMatch(declaredView::isSubTypeOf)) {
				upperBounds.add(declared);
			}
		}
		for (Type wildcardBound : wildcardBounds) {
			TypeView wildcardBoundView = of(wildcardBound);
			if (!(wildcardBound instanceof Class<?>)
					|| Stream.of(declarationBounds)
							.filter(d -> !(d instanceof Class<?>))
							.noneMatch(wildcardBoundView::isSubTypeOf)) {
				upperBounds.add(wildcardBound);
			}
		}
		if (upperBounds.isEmpty()) {
			upperBounds.add(Object.class);
		}
		Type[] upperBoundsArray = upperBounds.toArray(new Type[0]);
		SyntheticWildcardType resultType = new SyntheticWildcardType(type.getLowerBounds(), upperBoundsArray);
		return of(resultType);
	}

	@Override
	WildcardTypeView replaceVariables(VariableReplacer substitutions) {
		Type[] newLowerBounds = lowerBoundsStream()
			.map(bound -> bound.replaceVariables(substitutions))
			.map(TypeView::unwrap)
			.toArray(Type[]::new);
		Type[] newUpperBounds = upperBoundsStream()
			.map(bound -> bound.replaceVariables(substitutions))
			.map(TypeView::unwrap)
			.toArray(Type[]::new);
		if (Arrays.equals(type.getLowerBounds(), newLowerBounds)
			&& Arrays.equals(type.getUpperBounds(), newUpperBounds)) {
			return this;
		}
		SyntheticWildcardType newWildcard = new SyntheticWildcardType(newLowerBounds, newUpperBounds);
		return of(newWildcard);
	}

	@Override
	Type resolveVariable(TypeVariable<?> variable) {
		return Stream.concat(upperBoundsStream(), lowerBoundsStream())
			.map(bound -> bound.resolveVariable(variable))
			.filter(candidate -> !candidate.equals(variable))
			.findFirst()
			.orElse(variable);
	}

	@Override
	boolean containsVariant(TypeView other) {
		return lowerBoundsStream().allMatch(other::isSuperTypeOf)
			&& upperBoundsStream().allMatch(other::isSubTypeOf);
	}

	Stream<TypeView> lowerBoundsStream() {
		return Stream.of(type.getLowerBounds()).map(TypeView::of);
	}

	Stream<TypeView> upperBoundsStream() {
		Type[] upperBounds = type.getUpperBounds();
		return Stream.of(upperBounds).map(TypeView::of);
	}

	private static Type[] removeObjectClass(Type[] upperBounds) {
		return Stream.of(upperBounds).filter(bound -> !Object.class.equals(bound)).toArray(Type[]::new);
	}

	/**
	 * Mutable builder class for {@link WildcardTypeView}.
	 *
	 * <p>Create instances of this builder using {@link WildcardTypeView#builder}.
	 *
	 * <p>Although wildcards that are declared in code can have either at most one lower bound
	 * or at most one lower bound, nothing is really preventing runtime from creating wildcards that have multiples
	 * of both. This builder permits this behavior.
	 *
	 * <p>Produced type will have erasure equal to erasure of the first upper bound, or {@link Object} if no upper
	 * bounds were defined.
	 */
	public static final class Builder {
		private final List<Type> lowerBounds = new ArrayList<>();
		private final List<Type> upperBounds = new ArrayList<>();

		Builder() {
		}

		/**
		 * Adds lower bound to constructed type.
		 *
		 * <p>This is equivalent for adding {@code extends X} in code, where @{code X} is added bound.
		 *
		 * <p>Bounds will be returned in the same order as added in builder. Note that erasure of type variable is
		 * its first bound, or {@link Object} if there are no bounds.
		 *
		 * @param addedBound bound to add
		 * @return this, for chaining
		 */
		@CanIgnoreReturnValue
		public Builder withLowerBound(Type addedBound) {
			lowerBounds.add(addedBound);
			return this;
		}

		/**
		 * Adds upper bound to constructed type.
		 *
		 * <p>This is equivalent for adding {@code super X} in code, where @{code X} is added bound.
		 *
		 * <p>Bounds will be returned in the same order as added in builder.
		 *
		 * @param addedBound bound to add
		 * @return this, for chaining
		 */
		@CanIgnoreReturnValue
		public Builder withUpperBound(Type addedBound) {
			upperBounds.add(addedBound);
			return this;
		}

		/**
		 * Creates wildcard type configured from this builder.
		 *
		 * @return configured wildcard type as view
		 */
		public WildcardTypeView build() {
			@SuppressWarnings("assignment.type.incompatible")
			Type[] finalLowerBounds = lowerBounds.toArray(new Type[0]);
			@SuppressWarnings("assignment.type.incompatible")
			Type[] finalUpperBounds = upperBounds.toArray(new Type[0]);
			WildcardType type = new SyntheticWildcardType(finalLowerBounds, finalUpperBounds);
			return new WildcardTypeView(type);
		}
	}
}
