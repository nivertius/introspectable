package org.perfectable.introspection.type;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static com.google.common.base.Preconditions.checkState;

/**
 * {@link TypeView} that handles {@link GenericArrayType}.
 */
public final class ArrayTypeView extends AbstractTypeView<GenericArrayType> {

	ArrayTypeView(GenericArrayType type) {
		super(type);
	}

	/**
	 * Creates view over provided type.
	 *
	 * @param genericArray construct to wrap
	 * @return view wrapping specified type
	 */
	public static ArrayTypeView of(GenericArrayType genericArray) {
		return new ArrayTypeView(genericArray);
	}

	@Override
	public Class<?> erasure() {
		Class<?> componentErasure = component().erasure();
		return makeArrayClass(componentErasure);
	}

	/**
	 * Extracts array component type.
	 *
	 * <p>In essence, if array is of type {@code T[]} this method returns {@code T}.
	 *
	 * @return array component type.
	 */
	public TypeView component() {
		return of(type.getGenericComponentType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This method only works if component type is actually a class.
	 *
	 * @return parameterized view with base class equivalent to this array type and no substitutions
	 */
	@Override
	public ParameterizedTypeView asParameterized() {
		return asClass().asParameterized();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This method only works if component type is actually a class.
	 *
	 * @return class view with class equivalent to this array type.
	 */
	@Override
	public ClassView<?> asClass() {
		Type componentType = type.getGenericComponentType();
		checkState(componentType instanceof Class<?>, "Component is not a class, it's %s", componentType);
		Class<?> arrayClass = makeArrayClass((Class<?>) componentType);
		return new ClassView<>(arrayClass);
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to type variable.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a array type, and this call will fail
	 */
	@Deprecated
	@Override
	public TypeVariableView<?> asVariable() throws IllegalStateException {
		throw new IllegalStateException("Array type cannot be converted to variable");
	}

	/**
	 * Always throws {@link IllegalStateException}, because it is statically provable that this instance cannot
	 * be converted to wildcard type.
	 *
	 * @return never
	 * @throws IllegalStateException always
	 * @deprecated the type system already knows this is a array type, and this call will fail
	 */
	@Deprecated
	@Override
	public WildcardTypeView asWildcard() throws IllegalStateException {
		throw new IllegalStateException("Array type cannot be converted to wildcard");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return this, as this is already array type view
	 * @deprecated the type system already knows this is a array type, and this call does not change the type
	 */
	@Deprecated
	@Override
	public ArrayTypeView asArray() {
		return this;
	}

	@Override // SUPPRESS MethodLength
	public boolean isSubTypeOf(TypeView other) {
		return other.visit(new PartialVisitor<Boolean>() { // SUPPRESS AnonInnerLength
			@Override
			public Boolean visitParameterized(ParameterizedTypeView view) {
				if (!view.arguments().isEmpty()) {
					return false;
				}
				Class<?> raw = view.erasure();
				return isRawSubtype(raw);
			}

			@Override
			public Boolean visitClass(ClassView<?> view) {
				Class<?> raw = view.unwrap();
				if (Object.class.equals(raw)) {
					return true;
				}
				return isRawSubtype(raw);
			}

			@Override
			public Boolean visitWildcard(WildcardTypeView view) {
				return view.lowerBoundsStream().anyMatch(bound -> bound.isSuperTypeOf(type));
			}

			@Override
			public Boolean visitArray(ArrayTypeView view) {
				return component().isSubTypeOf(view.component());
			}

			@Override
			protected Boolean fallback() {
				return false;
			}

			private boolean isRawSubtype(Class<?> raw) {
				if (!raw.isArray()) {
					return false;
				}
				return component().isSubTypeOf(raw.getComponentType());
			}
		});
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitArray(this);
	}

	@Override
	public ArrayTypeView resolve(TypeView other) {
		return component().resolve(other).makeArray();
	}

	@Override
	public ArrayTypeView resolve(Type other) {
		return (ArrayTypeView) super.resolve(other);
	}

	@Override
	TypeView replaceVariables(VariableReplacer substitutions) {
		TypeView component = component();
		TypeView replaced = component.replaceVariables(substitutions);
		return replaced.equals(component) ? this : of(new SyntheticGenericArrayType(replaced.unwrap()));
	}

	@Override
	Type resolveVariable(TypeVariable<?> variable) {
		return component().resolveVariable(variable);
	}

	private static Class<?> makeArrayClass(Class<?> componentErasure) {
		return Array.newInstance(componentErasure, 0).getClass();
	}
}
