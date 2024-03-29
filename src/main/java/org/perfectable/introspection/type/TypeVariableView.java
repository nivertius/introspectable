package org.perfectable.introspection.type;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.InlineMe;

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

	/**
	 * Starts unconfigured builder for type variable.
	 *
	 * @param name name of variable about to be built
	 * @param declaration source that will be used as variable declartion point
	 * @param <D> type of declaration point for variable
	 * @return fresh builder, configured only with provided parameters
	 */
	public static <D extends GenericDeclaration> Builder<D> builder(String name, D declaration) {
		return new Builder<>(name, declaration);
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
	@DoNotCall
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
	@DoNotCall
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
	@InlineMe(replacement = "this")
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
	@DoNotCall
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
	@DoNotCall
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

	/**
	 * Mutable builder pattern for {@link TypeVariableView}.
	 *
	 * <p>Create instances of this builder using {@link TypeVariableView#builder}.
	 *
	 * <p>Normally, in code type variables can contain only one upper bound that is class, but nothing is preventing
	 * runtime from using multiples, and this builder doesn't have this restriction.
	 *
	 * <p>Produced type will have erasure equal to erasure of the first upper bound, or {@link Object} if no upper
	 * bounds were defined.
	 *
	 * @param <D> source that will be used for declaring the variable
	 */
	public static final class Builder<D extends GenericDeclaration> {
		private final D declaration;
		private final String name;
		private final List<Type> bounds = new ArrayList<>();

		Builder(String name, D declaration) {
			this.name = name;
			this.declaration = declaration;
		}

		/**
		 * Adds additional upper bound for type variable.
		 *
		 * <p>This is equivalent to writing in code {@code extends X} where {@code X} is a added bound.
		 *
		 * <p>Bounds will be returned in the same order as added in builder. Note that erasure of type variable is
		 * its first bound, or {@link Object} if there are no bounds.
		 *
		 * @param addedBound added bound
		 * @return this, for chaining
		 */
		@CanIgnoreReturnValue
		public Builder<D> withUpperBound(Type addedBound) {
			bounds.add(addedBound);
			return this;
		}

		/**
		 * Builds configured type variable.
		 *
		 * @return variable configured in this builder
		 */
		public TypeVariableView<D> build() {
			@SuppressWarnings("assignment.type.incompatible")
			Type[] finalBounds = bounds.toArray(new Type[0]);
			TypeVariable<D> typeVariable = new SyntheticTypeVariable<>(name, declaration, finalBounds);
			return new TypeVariableView<>(typeVariable);
		}
	}
}
