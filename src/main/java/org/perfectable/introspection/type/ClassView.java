package org.perfectable.introspection.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link TypeView} that handles {@link Class}.
 *
 * @param <X> program type that this view represents.
 */
public final class ClassView<X> extends AbstractTypeView<Class<X>> {
	ClassView(Class<X> type) {
		super(type);
	}

	/**
	 * Creates view over provided type.
	 *
	 * @param clazz construct to wrap
	 * @param <X> actual type represented by this class
	 * @return view wrapping specified type
	 */
	public static <X> ClassView<X> of(Class<X> clazz) {
		return new ClassView<>(clazz);
	}

	/**
	 * Allows cast-less conversion from raw class to either generic form, or parametrized class.
	 *
	 * <p>This function exists, because creating {@link ClassView} from class literal will produce view with raw type as
	 * argument. When provided literal is a generic class, produced type should parameterized with unbounded wildcards,
	 * but isn't. This method allows adjusting the type easily.
	 *
	 * <p>Example:
	 * <pre>
	 * ClassView&lt;List&gt; rawView = ClassView.of(List.class);
	 * ClassView&lt;List&lt;?&gt;&gt; genericView = rawView.adjustWildcards();
	 * </pre>
	 *
	 * <p>This method is equivalent to just casting to parameterized class with wildcards,
	 * but without unchecked warning.
	 *
	 * <p>WARNING: This method can be used to cast to inheriting types, i.e. {@code ClassView<ArrayList<Number>>} in
	 * previous example. If you are concerned that this might be the case, avoid this method, its only for convenience.
	 *
	 * @param <S> parameterized type to cast to
	 * @return casted class view
	 */
	@SuppressWarnings("unchecked")
	public <S extends X> ClassView<S> adjustWildcards() {
		return (ClassView<S>) this;
	}

	@Override
	public Class<X> erasure() {
		return type;
	}

	/**
	 * Extracts type variables that class declares.
	 *
	 * <p>This is empty if class is non-generic.
	 *
	 * @return variables declared by class
	 */
	public List<TypeVariableView<Class<X>>> parameters() {
		return Stream.of(type.getTypeParameters())
			.map(TypeView::of)
			.collect(Collectors.toList());
	}

	/**
	 * Converts wrapped class to {@link ParameterizedType}.
	 *
	 * <p>This always can be done, but the results might not be expected. Non-generic classes are be treated as generic
	 * class with zero type parameters. Generic classes are represented as parametrized type with the raw type being
	 * wrapped class and type arguments are copied from this class type parameters.
	 *
	 * <p>Resulting parameterized type is purely synthetic - it can never be actually declared or used in code.
	 *
	 * @return this view converted to view for parameterized type
	 */
	@Override
	public ParameterizedTypeView asParameterized() {
		Type[] typeArguments = type.getTypeParameters();
		@Nullable Class<?> ownerType = type.getEnclosingClass();
		SyntheticParameterizedType replaced = new SyntheticParameterizedType(type, ownerType, typeArguments);
		return new ParameterizedTypeView(replaced);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return this, as this is already class view
	 * @deprecated the type system already knows this is a class, and this call does not change the type
	 */
	@Deprecated
	@Override
	public ClassView<?> asClass() {
		return this;
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to type variable.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a class view, and this call will fail
	 */
	@Deprecated
	@Override
	public TypeVariableView<?> asVariable() throws IllegalStateException {
		throw new IllegalStateException("Class cannot be converted to variable");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to wildcard type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a class view, and this call will fail
	 */
	@Deprecated
	@Override
	public WildcardTypeView asWildcard() throws IllegalStateException {
		throw new IllegalStateException("Class cannot be converted to wildcard");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This method only works if wrapped class represents an array.
	 *
	 * @return this view casted/converted to view for array
	 * @throws IllegalStateException when this view wraps a class that's not an array
	 */
	@Override
	public ArrayTypeView asArray() throws IllegalStateException {
		@Nullable Class<?> componentType = type.getComponentType();
		if (componentType == null) {
			throw new IllegalStateException("Type has no array component");
		}
		@NonNull Class<?> checkedComponentType = componentType;
		SyntheticGenericArrayType arrayType = new SyntheticGenericArrayType(checkedComponentType);
		return new ArrayTypeView(arrayType);
	}

	@SuppressWarnings("type.argument.type.incompatible")
	@Override
	public boolean isSubTypeOf(TypeView other) {
		return other.visit(new Visitor<Boolean>() {
			@Override
			public Boolean visitParameterized(ParameterizedTypeView view) {
				return asParameterized().isSubTypeOf(view);
			}

			@Override
			public Boolean visitClass(ClassView<?> view) {
				return view.type.isAssignableFrom(type);
			}

			@Override
			public Boolean visitVariable(TypeVariableView<?> view) {
				return false;
			}

			@Override
			public Boolean visitWildcard(WildcardTypeView view) {
				return view.lowerBoundsStream().anyMatch(bound -> bound.isSuperTypeOf(type));
			}

			@Override
			public Boolean visitArray(ArrayTypeView view) {
				if (!type.isArray()) {
					return false;
				}
				@NonNull Class<?> componentType = (@NonNull Class<?>) type.getComponentType();
				return of(componentType).isSubTypeOf(view.component());
			}
		});
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitClass(this);
	}

	@Override
	public ParameterizedTypeView resolve(Type other) {
		return asParameterized().resolve(other);
	}

	@Override
	public ParameterizedTypeView resolve(TypeView other) {
		return asParameterized().resolve(other);
	}

	@Override
	TypeView replaceVariables(VariableReplacer replacer) {
		ParameterizedTypeView parameterized = asParameterized();
		ParameterizedTypeView replaced = parameterized.replaceVariables(replacer);
		return replaced.equals(parameterized) ? this : replaced;
	}

	@Override
	Type resolveVariable(TypeVariable<?> variable) {
		return asParameterized().resolveVariable(variable);
	}

	@Override
	boolean containsVariant(TypeView other) {
		return other.visit(new PartialVisitor<Boolean>() {
			@Override
			public Boolean visitClass(ClassView<?> view) {
				return view.type.equals(type);
			}

			@Override
			public Boolean visitWildcard(WildcardTypeView view) {
				return view.lowerBoundsStream().map(TypeView::unwrap).allMatch(type::equals)
					|| view.upperBoundsStream().map(TypeView::unwrap).allMatch(type::equals);
			}

			@Override
			protected Boolean fallback() {
				return false;
			}
		});
	}
}
