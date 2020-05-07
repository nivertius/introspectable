package org.perfectable.introspection.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@link TypeView} that handles {@link ParameterizedType}.
 */
@SuppressWarnings("ClassFanOutComplexity")
public final class ParameterizedTypeView extends AbstractTypeView<ParameterizedType> {
	private @MonotonicNonNull ImmutableMap<TypeVariable<? extends Class<?>>, Type> calculatedSubstitutions;
	private @MonotonicNonNull ImmutableList<TypeView> correctedArguments;

	ParameterizedTypeView(ParameterizedType parameterizedType) {
		super(parameterizedType);
	}

	/**
	 * Creates view over provided type.
	 *
	 * @param parameterized construct to wrap
	 * @return view wrapping specified type
	 */
	public static ParameterizedTypeView of(ParameterizedType parameterized) {
		return new ParameterizedTypeView(parameterized);
	}

	@Override
	public Class<?> erasure() {
		return (Class<?>) type.getRawType();
	}

	/**
	 * Calculates types used as arguments for this parameterized type.
	 *
	 * <p>In other words, this method returns list of types that were used in place of type variables in declaration
	 * of this type. For example, if wrapped type is {@code Map<Integer, ? extends String[]>} then this method
	 * will return a list with two elements containing type view wrapping of class and type view wrapping of wildcard
	 * type with array class as a upper bound.
	 *
	 * <p>This method creates <i>corrected</i> arguments: if raw type that this parameterized type is based on declared
	 * one of its variable to have upper bound, and in type argument substitution there were additional bounds declared,
	 * corrected argument will have both bounds. Ex. if raw type was a class with declaration
	 * {@code class Example<T extends Serializable> {...} } and this was used as field
	 * {@code private Example<? super String> field;}, then this method will return wildcard with both bounds:
	 * {@code ? extends Serializable super String}.
	 *
	 * @return types used as arguments for this parameterized type.
	 */
	@EnsuresNonNull("correctedArguments")
	public List<TypeView> arguments() {
		if (correctedArguments == null) {
			correctedArguments = calculateArguments(type);
		}
		return correctedArguments;
	}

	/**
	 * Extracts superclass as a type view.
	 *
	 * @return superclass wrapped in {@link TypeView}, empty if this class has no superclass
	 */
	public Optional<ParameterizedTypeView> superclass() {
		@Nullable Type genericSuperclass = erasure().getGenericSuperclass();
		if (genericSuperclass == null) {
			return Optional.empty();
		}
		VariableReplacer replacer = VariableReplacer.map(substitutions());
		ParameterizedTypeView result = of(genericSuperclass).replaceVariables(replacer).asParameterized();
		return Optional.of(result);
	}

	/**
	 * Extracts list of interfaces either extended or implemented by this type.
	 *
	 * @return interfaces wrapped in {@link TypeView}
	 */
	@SuppressWarnings("MutableMethodReturnType")
	public List<ParameterizedTypeView> interfaces() {
		ImmutableList.Builder<ParameterizedTypeView> builder = ImmutableList.builder();
		for (Type genericInterface : erasure().getGenericInterfaces()) {
			ParameterizedTypeView addedInterface = of(genericInterface)
				.replaceVariables(VariableReplacer.map(substitutions())).asParameterized();
			builder.add(addedInterface);
		}
		return builder.build();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return this, as this is already parameterized view
	 * @deprecated the type system already knows this is a parameterized type, and this call does not change the type
	 */
	@Deprecated
	@Override
	public ParameterizedTypeView asParameterized() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This method only works if this type has no actual substitutions and its base type is a class.
	 *
	 * @return class view with class equivalent to this unparameterized type
	 */
	@Override
	public ClassView<?> asClass() throws IllegalStateException {
		Type baseType = type.getRawType();
		checkState(baseType instanceof Class<?>, "Base type is not a class, it's %s", baseType);
		ImmutableMap<TypeVariable<? extends Class<?>>, Type> substitutions = substitutions();
		checkState(substitutions.isEmpty(), "There are substitutions defined in this type, %s", substitutions);
		Class<?> rawType = (Class<?>) baseType;
		return ClassView.of(rawType);
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to type variable.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a parameterized type, and this call will fail
	 */
	@Deprecated
	@Override
	public TypeVariableView<?> asVariable() throws IllegalStateException {
		throw new IllegalStateException("Parameterized type cannot be converted to type variable");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to wildcard type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a parameterized type, and this call will fail
	 */
	@Deprecated
	@Override
	public WildcardTypeView asWildcard() throws IllegalStateException {
		throw new IllegalStateException("Parameterized type cannot be converted to wildcard type");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This method only works if this type has no actual substitutions and its base type is an array class.
	 *
	 * @return class view with array class equivalent to this unparameterized type
	 */
	@Override
	public ArrayTypeView asArray() throws IllegalStateException {
		return asClass().asArray();
	}

	@SuppressWarnings({"AnonInnerLength", "MethodLength", "JavaNCSS"})
	@Override
	public boolean isSubTypeOf(TypeView other) {
		return other.visit(new Visitor<Boolean>() {
			@Override
			public Boolean visitParameterized(ParameterizedTypeView view) {
				Class<?> thisRaw = erasure();
				Class<?> otherRaw = view.erasure();
				if (!otherRaw.isAssignableFrom(thisRaw)) {
					return false;
				}
				if (otherRaw.isArray()) {
					@NonNull Class<?> componentType = (@NonNull Class<?>) otherRaw.getComponentType();
					return checkArray(thisRaw, of(componentType));
				}
				if (otherRaw.equals(thisRaw)) {
					Iterator<TypeView> thisParameterIterator = arguments().iterator();
					Iterator<TypeView> otherParameterIterator = view.arguments().iterator();
					while (thisParameterIterator.hasNext()) {
						TypeView thisParameter = thisParameterIterator.next();
						TypeView otherParameter = otherParameterIterator.next();
						if (!otherParameter.containsVariant(thisParameter)) {
							return false;
						}
					}
					return true;
				}
				return checkSupertypes(otherRaw, other);
			}

			@Override
			public Boolean visitClass(ClassView<?> view) {
				Type thisRaw = type.getRawType();
				if (!view.isSuperTypeOf(thisRaw)) {
					return false;
				}
				Class<?> otherRaw = view.erasure();
				if (otherRaw == Object.class) {
					return true;
				}
				if (otherRaw.isArray()) {
					if (!(thisRaw instanceof Class<?>)) {
						return false;
					}
					Class<?> thisClass = (Class<?>) thisRaw;
					Class<?> componentType = (@NonNull Class<?>) otherRaw.getComponentType();
					return checkArray(thisClass, of(componentType));
				}
				if (otherRaw.equals(thisRaw)) {
					return true;
				}
				return checkSupertypes(otherRaw, other);
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
				Type thisRaw = type.getRawType();
				if (!(thisRaw instanceof Class<?>)) {
					return false;
				}
				return checkArray((Class<?>) thisRaw, view.component());
			}

			private boolean checkSupertypes(Class<?> otherRaw, TypeView other) {
				Optional<ParameterizedTypeView> superclass = superclass();
				Stream<? extends TypeView> superStream = superclass.map(Stream::of).orElseGet(Stream::empty);
				Stream<? extends TypeView> checked =
					otherRaw.isInterface() ? interfaces().stream() : superStream;
				return checked.anyMatch(candidate -> candidate.isSubTypeOf(other));
			}

			private boolean checkArray(Class<?> thisRaw, TypeView otherComponent) {
				return thisRaw.isArray()
					&& arguments().isEmpty()
					&& otherComponent.isSuperTypeOf(thisRaw.getComponentType());
			}
		});
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitParameterized(this);
	}

	@Override
	public ParameterizedTypeView resolve(Type other) {
		return (ParameterizedTypeView) super.resolve(other);
	}

	@Override
	public ParameterizedTypeView resolve(TypeView other) {
		return (ParameterizedTypeView) super.resolve(other);
	}

	@Override
	ParameterizedTypeView replaceVariables(VariableReplacer replacer) {
		Type[] actualArguments = type.getActualTypeArguments();
		Type[] updatedArguments = new Type[actualArguments.length];
		System.arraycopy(actualArguments, 0, updatedArguments, 0, actualArguments.length);
		for (int i = 0; i < actualArguments.length; i++) {
			Type candidate = actualArguments[i];
			Type replacement = replacer.replacementFor(candidate);
			updatedArguments[i] = replacement;
		}
		ParameterizedType updated =
			new SyntheticParameterizedType((Class<?>) type.getRawType(), type.getOwnerType(), updatedArguments);
		return of(updated);
	}

	@Override
	Type resolveVariable(TypeVariable<?> variable) {
		ImmutableMap<? extends TypeVariable<? extends Class<?>>, Type> subs = substitutions();
		if (subs.containsKey(variable)) {
			return subs.get(variable);
		}
		for (ParameterizedTypeView genericInterface : interfaces()) {
			Type resolved = genericInterface.resolveVariable(variable);
			if (!resolved.equals(variable)) {
				return resolved;
			}
		}
		Optional<ParameterizedTypeView> genericSuperclass = superclass();
		if (genericSuperclass.isPresent()) {
			return genericSuperclass.get().resolveVariable(variable);
		}
		return variable;
	}

	@EnsuresNonNull("calculatedSubstitutions")
	private ImmutableMap<TypeVariable<? extends Class<?>>, Type> substitutions() {
		if (calculatedSubstitutions == null) {
			calculatedSubstitutions = calculateSubstitutions(type);
		}
		return calculatedSubstitutions;
	}

	private static ImmutableMap<TypeVariable<? extends Class<?>>, Type> calculateSubstitutions(ParameterizedType type) {
		Class<?> rawType = (Class<?>) type.getRawType();
		ImmutableMap.Builder<TypeVariable<? extends Class<?>>, Type> substitutionsBuilder = ImmutableMap.builder();
		TypeVariable<? extends Class<?>>[] typeParameters = rawType.getTypeParameters();
		for (int i = 0; i < typeParameters.length; i++) {
			TypeVariable<? extends Class<?>> typeParameter = typeParameters[i];
			Type actualTypeArgument = type.getActualTypeArguments()[i];
			if (!actualTypeArgument.equals(typeParameter)) {
				substitutionsBuilder.put(typeParameter, actualTypeArgument);
			}
		}
		return substitutionsBuilder.build();
	}

	private static ImmutableList<TypeView> calculateArguments(ParameterizedType type) {
		TypeVariable<? extends Class<?>>[] parameterDeclarations = ((Class<?>) type.getRawType()).getTypeParameters();
		Type[] actualArguments = type.getActualTypeArguments();
		ImmutableList.Builder<TypeView> builder = ImmutableList.builder();
		for (int i = 0; i < parameterDeclarations.length; i++) {
			Type actualArgument = actualArguments[i];
			TypeVariable<? extends Class<?>> parameter = parameterDeclarations[i];
			TypeView argumentView = of(actualArgument).declaredAs(parameter);
			builder.add(argumentView);
		}
		return builder.build();
	}
}
