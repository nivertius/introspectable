package org.perfectable.introspection.type; // SUPPRESS FILE FileLength

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Simplified and more useful handle of java {@link Type}.
 *
 * <p>This type is a facade over {@link Type} and its all five descendants, that offers unified interface to them.
 *
 * <p>It allows easy tests for it:
 * <ul>
 *     <li>Subtyping and supertyping</li>
 *     <li>Getting actual erasure</li>
 * </ul>
 *
 * <p>In addition, this type allows to "resolve" one type in context of another. Resolving means using variable
 * replacements of context type in with current type. See {@link #resolve}.
 *
 * <p>Instances of this type are immutable, as long as provided implementations of {@link Type} and its descendant are.
 * Plain java constructs of those are immutable.
 */
@SuppressWarnings({
	"DesignForExtension", // class is closed because of package-private constructor
	"MethodCount"
})
public abstract class TypeView { // SUPPRESS MethodCount

	/**
	 * Creates view over provided type.
	 *
	 * @param type construct to wrap
	 * @return view wrapping specified type
	 */
	public static TypeView of(Type type) {
		if (type instanceof ParameterizedType) {
			return of((ParameterizedType) type);
		}
		if (type instanceof Class<?>) {
			return of((Class<?>) type);
		}
		if (type instanceof TypeVariable<?>) {
			return new TypeVariableView<>((TypeVariable<?>) type);
		}
		if (type instanceof WildcardType) {
			return new WildcardTypeView((WildcardType) type);
		}
		if (type instanceof GenericArrayType) {
			return of((GenericArrayType) type);
		}
		throw new AssertionError("Unknown type: " + type);
	}

	/**
	 * Creates view over provided type.
	 *
	 * <p>This is a overload when argument is known to be {@link ParameterizedType},
	 * to return {@link ParameterizedTypeView}
	 *
	 * @param parameterized construct to wrap
	 * @return view wrapping specified type
	 */
	public static ParameterizedTypeView of(ParameterizedType parameterized) {
		return ParameterizedTypeView.of(parameterized);
	}

	/**
	 * Creates view over provided type.
	 *
	 * <p>This is a overload when argument is known to be {@link Class}, to return {@link ClassView}
	 *
	 * @param clazz construct to wrap
	 * @param <X> actual type represented by this class
	 * @return view wrapping specified type
	 */
	public static <X> ClassView<X> of(Class<X> clazz) {
		return ClassView.of(clazz);
	}

	/**
	 * Creates view over provided type.
	 *
	 * <p>This is a overload when argument is known to be {@link TypeVariable}, to return {@link TypeVariableView}
	 *
	 * @param variable construct to wrap
	 * @param <D> element type that is declaring this variable
	 * @return view wrapping specified type
	 */
	public static <D extends GenericDeclaration> TypeVariableView<D> of(TypeVariable<D> variable) {
		return TypeVariableView.of(variable);
	}

	/**
	 * Creates view over provided type.
	 *
	 * <p>This is a overload when argument is known to be {@link WildcardType}, to return {@link WildcardTypeView}
	 *
	 * @param wildcard construct to wrap
	 * @return view wrapping specified type
	 */
	public static WildcardTypeView of(WildcardType wildcard) {
		return WildcardTypeView.of(wildcard);
	}

	/**
	 * Creates view over provided type.
	 *
	 * <p>This is a overload when argument is known to be {@link GenericArrayType}, to return {@link ArrayTypeView}
	 *
	 * @param genericArray construct to wrap
	 * @return view wrapping specified type
	 */
	public static ArrayTypeView of(GenericArrayType genericArray) {
		return ArrayTypeView.of(genericArray);
	}

	/**
	 * Returns view of corrected type for declared field.
	 *
	 * @param field to extract type from
	 * @return view of type of declared field
	 */
	public static TypeView ofTypeOf(Field field) {
		return of(field.getGenericType());
	}

	/**
	 * Returns view of n-th type parameter declared in method.
	 *
	 * <p>This extracts exactly <b>type parameters</b> that are declared with method, not type of method
	 * formal parameter.
	 *
	 * <p>In following example, {@code ofTypeParameterOf(<m>, 0)} returns {@code E}, not {@code String}.
	 * <pre>
	 *     public &lt;E&gt; void m(String i, E j)
	 * </pre>
	 * 
	 * <p>For extraction of type of formal parameter, see {@link #ofParameterOf}.
	 *
	 * @param method to extract type from
	 * @param number ordinal number for searched type parameter, counted from {@code 0}
	 * @return view of type parameter
	 * @throws IllegalArgumentException when type parameter number {@code number} is not within
	 *     method type parameter range
	 * @see #ofParameterOf
	 */
	public static TypeVariableView<Method> ofTypeParameterOf(Method method, int number)
			throws IllegalArgumentException {
		TypeVariable<Method> parameter = safeAccessArray(method.getTypeParameters(), number);
		return of(parameter);
	}

	/**
	 * Returns view of n-th formal parameter declared in method.
	 *
	 * <p>This extracts exactly <b>formal parameters</b> of method, not type of type parameter declared by method.
	 *
	 * <p>In following example, {@code ofTypeParameterOf(<m>, 0)} returns {@code String}, not {@code E}.
	 * <pre>
	 *     public &lt;E&gt; void m(String i, E j)
	 * </pre>
	 *
	 * <p>For extraction of type parameter, see {@link #ofTypeParameterOf}.
	 *
	 * @param method to extract type from
	 * @param number ordinal number for searched formal parameter, counted from {@code 0}
	 * @return view of type of formal parameter
	 * @throws IllegalArgumentException when type parameter number {@code number} is not within
	 *     method formal parameter range
	 * @see #ofTypeParameterOf
	 */
	public static TypeView ofParameterOf(Method method, int number) {
		Type parameter = safeAccessArray(method.getGenericParameterTypes(), number);
		return of(parameter);
	}

	TypeView() {
		// package-only extension
	}

	/**
	 * Extracts native type from this view.
	 *
	 * <p>This is inverse operation from {@link #of(Type)}
	 *
	 * @return type that this view was for
	 */
	public abstract Type unwrap();

	/**
	 * Finds exact class that would be erasure of wrapped type.
	 *
	 * <p>This method is implemented according to JSL section 4.6 with following extensions:
	 * <ul>
	 *     <li>Erasure of wildcard type is erasure of its leftmost upper bound,
	 *     		or {@link Object} if there isn't one</li>
	 * </ul>
	 *
	 * <p>These extensions are needed, because these types are cannot be used in declaration, but can be obtained by
	 * reflections.
	 *
	 * @return erasure of wrapped type
	 */
	public abstract Class<?> erasure();

	/**
	 * Tests if type wrapped in this view is subtype of the provided one.
	 *
	 * <p>This method tries to adhere to the spirit of subtyping: it answers the question:
	 * Having reference of type T, can value of type S be always assigned to it?
	 *
	 * <p>Subtyping is precisely defined for types that can be declared in source, but there are more types that can
	 * be constructed or extracted via reflections. This method follows JLS section 4.10 for every aspect it declares,
	 * but also defines sensible rule for subtyping wildcards: wildcard is a supertype of its lower bounds,
	 * i.e. {@code ? super CharStream} is supertype of CharStream and
	 *
	 * @param other type to test subtype relation against
	 * @return true if this type is subtype of provided argument
	 */
	public abstract boolean isSubTypeOf(TypeView other);

	/**
	 * Tests if type wrapped in this view is subtype of the provided one.
	 *
	 * <p>See {@link #isSubTypeOf} for more.
	 *
	 * @param other type to test subtype relation against
	 * @return true if this type is subtype of provided argument
	 */
	public boolean isSubTypeOf(Type other) {
		return isSubTypeOf(of(other));
	}

	/**
	 * Tests if type wrapped in this view is subtype of the provided one, but they are not the same type.
	 *
	 * <p>See {@link #isSubTypeOf} for more about testing how subtyping is calculated.
	 *
	 * @param other type to test subtype relation against
	 * @return true if this type is subtype of provided argument
	 */
	public boolean isProperSubtypeOf(TypeView other) {
		return !equals(other) && isSubTypeOf(other);
	}

	/**
	 * Tests if type wrapped in this view is subtype of the provided one, but they are not the same type.
	 *
	 * <p>See {@link #isSubTypeOf} for more about testing how subtyping is calculated.
	 *
	 * @param other type to test subtype relation against
	 * @return true if this type is subtype of provided argument
	 */
	public boolean isProperSubtypeOf(Type other) {
		return isProperSubtypeOf(of(other));
	}

	/**
	 * Tests if type wrapped in this view is supertype of the provided one.
	 *
	 * <p>Type T is supertype of S iff S is subtype of T.
	 *
	 * @param other type to test supertype relation against
	 * @return true if this type is supertype of provided argument
	 */
	public boolean isSuperTypeOf(TypeView other) {
		return other.isSubTypeOf(this);
	}

	/**
	 * Tests if type wrapped in this view is supertype of the provided one.
	 *
	 * <p>Type T is supertype of S iff S is subtype of T.
	 *
	 * @param other type to test supertype relation against
	 * @return true if this type is supertype of provided argument
	 */
	public boolean isSuperTypeOf(Type other) {
		return isSuperTypeOf(of(other));
	}

	/**
	 * Tests if type wrapped in this view is supertype of the provided one, but they are not the same type.
	 *
	 * <p>Type T is supertype of S iff S is subtype of T.
	 *
	 * @param other type to test supertype relation against
	 * @return true if this type is supertype of provided argument
	 */
	public boolean isProperSupertypeOf(TypeView other) {
		return !equals(other) && isSuperTypeOf(other);
	}

	/**
	 * Tests if type wrapped in this view is supertype of the provided one, but they are not the same type.
	 *
	 * <p>Type T is supertype of S iff S is subtype of T.
	 *
	 * @param other type to test supertype relation against
	 * @return true if this type is supertype of provided argument
	 */
	public boolean isProperSupertypeOf(Type other) {
		return isProperSupertypeOf(of(other));
	}

	/**
	 * Tests if provided value is instance of wrapped type.
	 *
	 * <p>Because of type erasure, this method only tests if erasure of provided element type is subtype of this type.
	 * This might not always be true! For example, for method {@code boolean m(List<? extends Number> t)}
	 * extracted parameter type is {@code List<? extends Number>} but {@code t} has runtime type {@code List}.
	 * If we have view of type {@code List<? extends Serializable>}, then {@code isInstance(t)} should
	 * return true, when in fact it will return false, as runtime class is treated as parameterized class with
	 * no substitutions.
	 *
	 * <p>As in plain java, {@code null} is subtype of only of null type, which cannot be obtained as a reference,
	 * so this will always return false for it.
	 *
	 * <p>See {@link #isSubTypeOf} for how subtyping is calculated.
	 *
	 * @param element type to test supertype relation against
	 * @return true if this type is supertype of provided argument
	 */
	public boolean isInstance(@Nullable Object element) {
		if (element == null) {
			return false;
		}
		return isSuperTypeOf(element.getClass());
	}

	/**
	 * Try to cast or convert wrapped type to {@link ParameterizedType}.
	 *
	 * <p>This method is useful if you know that underlying type is {@link ParameterizedType},
	 * then this is just a casting.
	 *
	 * <p>This can also be done for regular classes, {@link ClassView}, as non-generic class can be treated as
	 * generic class with zero type parameters. It also works for class-represented arrays that are currently converted
	 * to parameterized types.
	 *
	 * @return this view casted/converted to view for parameterized type
	 * @throws IllegalStateException when this view cannot be converted to ParameterizedView.
	 */
	public abstract ParameterizedTypeView asParameterized() throws IllegalStateException;

	/**
	 * Try to cast or convert wrapped type to {@link Class}.
	 *
	 * <p>This method is useful if you know that underlying type is {@link Class},
	 * then this is just a casting.
	 *
	 * <p>This can also be done for parameterized types with no actual substitutions and array classes represented by
	 * {@link ArrayTypeView}
	 *
	 * @return this view casted/converted to view for class
	 * @throws IllegalStateException when this view cannot be converted to Class.
	 */
	public abstract ClassView<?> asClass() throws IllegalStateException;

	/**
	 * Try to cast wrapped type to {@link TypeVariable}.
	 *
	 * <p>This method is useful if you know that underlying type is {@link Class},
	 * then this is just a casting.
	 *
	 * <p>For any other type, this throws {@link IllegalStateException}, because nothing else can be converted to type
	 * variable.
	 *
	 * @return this view casted to view for type variable
	 * @throws IllegalStateException when this view is not a view of TypeVariable.
	 */
	public abstract TypeVariableView<?> asVariable() throws IllegalStateException;

	/**
	 * Try to cast wrapped type to {@link WildcardType}.
	 *
	 * <p>This method is useful if you know that underlying type is {@link WildcardType},
	 * then this is just a casting.
	 *
	 * <p>For any other type, this throws {@link IllegalStateException}, because nothing else can be converted to type
	 * wildcard.
	 *
	 * @return this view casted to view for wildcard type
	 * @throws IllegalStateException when this view is not a view of WildcardType.
	 */
	public abstract WildcardTypeView asWildcard() throws IllegalStateException;

	/**
	 * Try to cast/convert wrapped type to {@link GenericArrayType}.
	 *
	 * <p>This method is useful if you know that underlying type is {@link GenericArrayType},
	 * then this is just a casting.
	 *
	 * <p>This can also be done for parameterized types with no actual substitutions that represetn array class,
	 * and class view that has array class.
	 *
	 * @return this view casted/converted to view for array
	 * @throws IllegalStateException when this view cannot be converted to view of GenericArrayType.
	 */
	public abstract ArrayTypeView asArray() throws IllegalStateException;

	/**
	 * Create view that has variable substitutions copied from provided.
	 *
	 * <p>See documentation on {@link #resolve(TypeView)} for details.
	 *
	 * @param other type to copy substitutions from
	 * @return TypeView that uses substitutions from provided type
	 */
	public TypeView resolve(Type other) {
		return resolve(of(other));
	}

	/**
	 * Create view that has variable substitutions copied from provided.
	 *
	 * <p>This method creates type that has any variable substituted by substitutions used in {@code other}.
	 *
	 * <p>Substitutions are found recursively. They can be only directly found in parameterized types, as arguments, but
	 * these parameterized types can be found in components of some containing type. For example, wildcard might use
	 * parameterized type as a bound, and this parameterized type might have substitution for specific variable.
	 *
	 * <p>This method can be used to find what the signature of field or method looks like in some specific type. For
	 * example: lets say that there's a generic class {@code class Example<T>} that declares field
	 * {@code protected List<T> elements}. It's descendant class, declared
	 * {@code class ExtendedExample extends Example<String>} inherits field {@code elements}. In this case no
	 * conventional method will extract actual field type, {@code List<String>}:
	 * <ul>
	 * <li>Field found by {@link Class#getDeclaredField} called on {@code ExampleExtension} will return null,
	 * as this field is not declared there.</li>
	 * <li>Field found by {@link Class#getDeclaredField} called on {@code Example} (or {@link Class#getField} on
	 * child class, if the field would be public) will return field from parent class,
	 * executing {@link Field#getGenericType} on it will return {@code List<T>}</li>
	 * </ul>
	 * But this can be resolved using this method:
	 * {@code TypeView.ofTypeOf(Example.class.getDeclaredField("elements")).resolve(ExampleExtension.class)} will wrap
	 * {@code List<String>}, because variable substitution {@code T} -> {@code String} is used.
	 *
	 * @param other type to copy substitutions from
	 * @return TypeView that uses substitutions from provided type
	 */
	public TypeView resolve(TypeView other) {
		VariableReplacer collector = VariableReplacer.view(other);
		return replaceVariables(collector);
	}

	/**
	 * Visitor patter method dispatching method.
	 *
	 * <p>This method calls specific method on provided {@code visitor}, depending on actual implementation of this
	 * type.
	 *
	 * <p>It will return whatever the method returns.
	 *
	 * @param visitor method target
	 * @param <T> type returned by visitor method
	 * @return same result as visitors method.
	 */
	public abstract <T> T visit(Visitor<T> visitor);

	/**
	 * Visitor pattern interface.
	 *
	 * @param <T> type returned by methods.
	 */
	public interface Visitor<T> {
		/**
		 * Called when visited type is {@link ParameterizedTypeView}.
		 *
		 * @param view casted {@link TypeView} that is visited
		 * @return value to be returned from {@link TypeView#visit}
		 */
		T visitParameterized(ParameterizedTypeView view);

		/**
		 * Called when visited type is {@link ClassView}.
		 *
		 * @param view casted {@link TypeView} that is visited
		 * @return value to be returned from {@link TypeView#visit}
		 */
		T visitClass(ClassView<?> view);

		/**
		 * Called when visited type is {@link TypeVariableView}.
		 *
		 * @param view casted {@link TypeView} that is visited
		 * @return value to be returned from {@link TypeView#visit}
		 */
		T visitVariable(TypeVariableView<?> view);

		/**
		 * Called when visited type is {@link WildcardTypeView}.
		 *
		 * @param view casted {@link TypeView} that is visited
		 * @return value to be returned from {@link TypeView#visit}
		 */
		T visitWildcard(WildcardTypeView view);

		/**
		 * Called when visited type is {@link ArrayTypeView}.
		 *
		 * @param view casted {@link TypeView} that is visited
		 * @return value to be returned from {@link TypeView#visit}
		 */
		T visitArray(ArrayTypeView view);
	}

	/**
	 * Implementation of {@link Visitor} that has default method.
	 *
	 * <p>This implementation by default dispatches every call to {@link #fallback} method, which must be implemented.
	 * This method should provided default result for unsupported {@link TypeView} implementations. Supported types
	 * should have analogous method overridden.
	 *
	 * @param <T> type returned by methods
	 */
	public abstract static class PartialVisitor<T> implements Visitor<T> {
		/**
		 * Called when method for specific {@link TypeView} was not overridden.
		 *
		 * @return value to be returned from {@link TypeView#visit}
		 */
		protected abstract T fallback();

		@Override
		public T visitParameterized(ParameterizedTypeView view) {
			return fallback();
		}

		@Override
		public T visitClass(ClassView<?> view) {
			return fallback();
		}

		@Override
		public T visitVariable(TypeVariableView<?> view) {
			return fallback();
		}

		@Override
		public T visitWildcard(WildcardTypeView view) {
			return fallback();
		}

		@Override
		public T visitArray(ArrayTypeView view) {
			return fallback();
		}
	}

	abstract TypeView replaceVariables(VariableReplacer substitutions);

	abstract Type resolveVariable(TypeVariable<?> variable);

	TypeView declaredAs(TypeVariable<?> parameter) {
		return this;
	}

	boolean containsVariant(TypeView other) {
		return equals(other);
	}

	ArrayTypeView makeArray() {
		SyntheticGenericArrayType arrayType = new SyntheticGenericArrayType(unwrap());
		return new ArrayTypeView(arrayType);
	}

	private static <T> T safeAccessArray(T[] elements, int number) {
		checkArgument(number >= 0);
		checkArgument(number < elements.length);
		return elements[number];
	}
}
