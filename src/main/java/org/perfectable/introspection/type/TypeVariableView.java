package org.perfectable.introspection.type;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link TypeView} that handles {@link TypeVariable}.
 *
 * @param <D> Type declaring this variable
 */
public final class TypeVariableView<D extends GenericDeclaration> extends AbstractTypeView<TypeVariable<D>> {
	TypeVariableView(TypeVariable<D> type) {
		super(type);
	}

	/**
	 * Creates view over provided type.
	 *
	 * @param variable construct to wrap
	 * @param <D> element type that is declaring this variable
	 * @return view wrapping specified type
	 */
	public static <D extends GenericDeclaration> TypeVariableView<D> of(TypeVariable<D> variable) {
		return new TypeVariableView<>(variable);
	}

	@Override
	public Class<?> erasure() {
		Type[] bounds = type.getBounds();
		if (bounds.length == 0) {
			return Object.class;
		}
		Type firstBound = bounds[0];
		return of(firstBound).erasure();
	}

	/**
	 * Extracts upper bounds for this variable.
	 *
	 * <p>Realizations of this variable must extend/implement all specified types.
	 *
	 * <p>In actual code, there can be only one upper bound, it is impossible to declare multiple bounds. But synthetic
	 * types can have multiple as reflection system supports it.
	 *
	 * <p>If variable has no upper bound, this method will return singleton collection with {@link Object} class.
	 * This is following contract in {@link TypeVariable#getBounds}, which will return singleton array with
	 * {@link Object} class.
	 *
	 * @return upper bounds of this variable, wrapped in {@link TypeView}
	 */
	public Collection<TypeView> upperBounds() {
		return upperBoundsStream()
			.collect(Collectors.toList());
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to parameterized type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a type variable, and this call will fail
	 */
	@Deprecated
	@Override
	public ParameterizedTypeView asParameterized() throws IllegalStateException {
		throw new IllegalStateException("Type variable cannot be converted to parameterized view");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to class view.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a type variable, and this call will fail
	 */
	@Deprecated
	@Override
	public ClassView<?> asClass() throws IllegalStateException {
		throw new IllegalStateException("Type variable cannot be converted to class");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return this, as this is already variable view
	 * @deprecated the type system already knows this is a variable, and this call does not change the type
	 */
	@Deprecated
	@Override
	public TypeVariableView<?> asVariable() {
		return this;
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to wildcard type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a type variable, and this call will fail
	 */
	@Deprecated
	@Override
	public WildcardTypeView asWildcard() throws IllegalStateException {
		throw new IllegalStateException("Type variable cannot be converted to wildcard");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to array type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a type variable, and this call will fail
	 */
	@Deprecated
	@Override
	public ArrayTypeView asArray() throws IllegalStateException {
		throw new IllegalStateException("Type variable cannot be converted to array type");
	}

	@Override
	public boolean isSubTypeOf(TypeView other) {
		return other.visit(new PartialVisitor<Boolean>() {
			@Override
			public Boolean visitVariable(TypeVariableView<?> view) {
				return view.type.equals(type) || fallback();
			}

			@Override
			public Boolean visitWildcard(WildcardTypeView view) {
				return view.lowerBoundsStream()
						.allMatch(wildcardBound ->
							upperBoundsStream().anyMatch(bound -> bound.isSubTypeOf(wildcardBound)))
					&& view.upperBounds().isEmpty();
			}

			@Override
			protected Boolean fallback() {
				return upperBoundsStream().anyMatch(bound -> bound.isSubTypeOf(other));
			}
		});
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitVariable(this);
	}

	@Override
	TypeView replaceVariables(VariableReplacer substitutions) {
		Type replaced = substitutions.replacementFor(type);
		if (!type.equals(replaced)) {
			return of(replaced);
		}
		Type[] originalBounds = type.getBounds();
		Type[] updatedBounds = type.getBounds().clone();
		boolean changed = false;
		for (int i = 0; i < originalBounds.length; i++) {
			Type candidate = originalBounds[i];
			Type replacement = substitutions.replacementFor(candidate);
			if (!candidate.equals(replacement)) {
				updatedBounds[i] = replacement;
				changed = true;
			}
		}
		if (!changed) {
			return this;
		}
		TypeVariable<D> updated =
			new SyntheticTypeVariable<>(type.getName(), type.getGenericDeclaration(), updatedBounds);
		return of(updated);
	}

	@Override
	Type resolveVariable(TypeVariable<?> variable) {
		return upperBoundsStream()
			.map(bound -> bound.resolveVariable(variable))
			.filter(candidate -> !candidate.equals(variable))
			.findFirst()
			.orElse(variable);
	}

	private Stream<TypeView> upperBoundsStream() {
		return Stream.of(type.getBounds())
			.map(TypeView::of);
	}
}
