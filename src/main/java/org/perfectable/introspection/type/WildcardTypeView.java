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

	@Override
	public Class<?> erasure() {
		Type[] bounds = type.getUpperBounds();
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
}
