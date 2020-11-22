package org.perfectable.introspection;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Interface that allows passing compile-time safe information around.
 *
 * <p>This interface, when extended by functional interface, can be used for passing references to methods or
 * constructors, or parameters of those between parts of program, with compile-time conformance checking enabled.
 *
 * <p>Sometimes, when one part of the program needs to identify some construct of the program that is not known to it
 * at time of its creation, we often use complete or partial reflection information to identify these constructs.
 * For example:
 * <ul>
 *     <li>Class literal, method name, and method parameter erasures literals to identify method</li>
 *     <li>Class literal and constructor parameter erasure literals to identify constructor</li>
 *     <li>Any of above and parameter index to identify type with additional annotations</li>
 * </ul>
 * In any of above case, some of the elements can be omitted if they are 'assumed' from context.
 * More concrete examples:
 * <ul>
 *     <li>Serialization framework needs a method of creating object of class C, if not by no-argument constructor,
 *     a static method name as string in the C class can be passed.</li>
 *     <li>Dynamic form library wants to set properties entered by user on bound object, so property name as string is
 *     passed.</li>
 *     <li>Dependency Injection framework needs a way to pass dependencies to newly created object, so it requires
 *     annotation type to be included when searching for qualifiers on each dependency.</li>
 * </ul>
 *
 * <p>Using this interface, above scenarios can be solved in clean, compile-time checked and refactor-safe ways.
 *
 * <p>To use this interface, declare a functional interface that has method with signature that user needs to provide.
 * This new interface needs to extend this one. Then, expect specified interface as a parameter for some method in place
 * where reflection information would be passed. Expect client of this method to pass a non-class based implementation
 * of this functional reference, preferably method/constructor reference, but lambda will also work if method name is
 * not required and only actual parameter types and annotations are required. Document that requirement. After that
 * on the passed interface implementation, execute {@link #introspect}, preferably once, and access expected information
 * from it.
 *
 * <p>Example:
 * <pre>
 *     interface Getter&lt;S, V&gt; extends FunctionalReference {
 *	         V get(S source);
 *     }
 *
 *     class DocumentCreator {
 *         public &lt;S&gt; useForSummaryExtraction(Getter&lt;S, String&gt; summaryExtractor) {
 *             FunctionalReference.Introspection introspection = summaryExtractor.introspect()
 *             String methodName = introspection.referencedMethodName();
 *             this.extractors.put(OutlinePart.SUMMARY, methodName);
 *         }
 *     }
 *
 *     class Person {
 *         String getSelfDescription() {  ... }
 *     }
 *
 *     class PersonResume {
 *         String generate(Person person) {
 *             DocumentCreator creator = ...
 *             creator.useForSummaryExtraction(Person::getSelfDescription);
 *         }
 *     }
 * </pre>
 *
 * <p>In a pinch, this method can be also used to extract refactor-safe constants. Unfortunately this is not very clean
 * way of representing it.
 * <pre>
 *     private static final String SUMMARY_GETTER_NAME =
 *         ((Getter&lt;Person,String&gt;) Person::getSelfDescription).introspect().getReferencedMethodName();
 * </pre>
 *
 * <p>Introspected reference can be on of either of five 'modes'. Three of those are method references,
 * one constructor reference and lambda. These modes are described in JSL section 15.13 and 15.27.
 * This class named the modes for convenience:
 * <ul>
 *     <li>Reference to static method (named "static"). This mode is expression in form
 *     {@literal ReferenceType::Identifier} and matches static method in {@literal ReferenceType}
 *     (ex. {@code System::identityHashCode})</li>
 *     <li>Reference to instance method without primary identifier (named "instance"). This mode is expression
 *     in form {@literal ReferenceType::Identifier} and matches instance method in {@literal ReferenceType}
 *     (ex {@code Object::hashCode})</li>
 *     <li>Reference to instance method with primary identifier (named "bound", as the instance will be bound to the
 *     reference). This mode is expression in form {@literal Primary::Identifier} and matches instance method in
 *     {@literal ReferenceType} (ex. {@code specificPerson::getSelfDescription})</li>
 *     <li>Reference to constructor (named "constructor"). This mode is expression in form
 *     {@literal ReferenceType::new} and matches constructor in {@literal Reference type} (ex. Person::new)</li>
 *     <li>Lambda expression (named "lambda").</li>
 * </ul>
 *
 * <p>Although this interface can be used to extract information while application is in running cycle, it is strongly
 * discouraged, because there is a performance hit any time the {@link #introspect} method is called. This should be
 * only used when starting application - to configure objects.
 *
 * <p>Using this method has some performance penalty, but it should be somewhat in range of using reflections in above
 * scenarios. When method/constructor references are used to pass information around, no additional program constructs
 * should be generated by the compiler. Lambdas will still produce additional synthetic method(s) in declaring class.
 */
public interface FunctionalReference extends Serializable {
	/**
	 * Analyzes reference and produces information access object.
	 *
	 * <p>This method has some performance penalty and should be called once on any given reference. Produced
	 * introspection is almost immutable, and for the same instance, this method will produce the same result. Almost
	 * immutable, because it references all of the captures in passed lambda and bound instance of bound methods,
	 * so if those are not immutable, the introspection also isn't.
	 *
	 * <p>This method will throw exception when the instance is not implemented by method/constructor reference
	 * or lambda (ex. it's implemented by class).
	 *
	 * <p>Do not override this method, as this is missing the whole point of this mechanism.
	 *
	 * @implSpec Introspection is done by using SerializedLambda trick: if method/constructor reference or lambda
	 *     implements {@link Serializable}, beside its invocation function, it also declares {@code writeReplace} method
	 *     (described in Serializable documentation), which returns {@link java.lang.invoke.SerializedLambda}.
	 *     This object can be interrogated to extract:
	 *     <ul>
	 *         <li>What kind of implementation is provided</li>
	 *         <li>Where was it declared</li>
	 *         <li>Whats the signature of the target method</li>
	 *         <li>If its a bound method, what is the referenced instance</li>
	 *         <li>If its a lambda, what are the captured objects</li>
	 *     </ul>
	 *     Using this information, and little reflection, all of the needed data can be extracted.
	 *
	 *     This behavior is documented in {@link java.lang.invoke.LambdaMetafactory#altMetafactory} and should be
	 *     consistent between implementations.
	 *
	 * @return introspection of passed reference
	 */
	default Introspection introspect() {
		return FunctionalReferenceIntrospection.of(this);
	}

	/**
	 * Introspection of functional reference.
	 *
	 * <p>WARNING: This interface is "consume only" from perspective of semantic versioning. Do not implement it,
	 * use its instances returned from {@link #introspect}. It might change in backward incompatible way for
	 * implementors (but not for clients) in minor version changes, for example by adding abstract method.
	 */
	interface Introspection {
		/**
		 * Visitor Pattern acceptor method.
		 *
		 * <p>This method dispatches type of provided reference to visitor, calling single method for each
		 * implementation kind.
		 *
		 * @param visitor visitor to dispatch into
		 * @param <T> type of visitor result
		 * @return whatever visitor call returned
		 */
		@CanIgnoreReturnValue
		<T> T visit(Visitor<T> visitor);

		/**
		 * Extracts class in which referenced method was declared.
		 *
		 * <p>For lambdas, the method is a synthetic and declared in place where lambda was declared.
		 *
		 * @return declaration class of the method reference
		 */
		Class<?> capturingType();

		/**
		 * Returns actual type returned by implementation method.
		 *
		 * <p>This might be different from function type return type which the reference implements, because
		 * implementation has a return-type-substitutable return type (JLS 8.4.5).
		 *
		 * @return actual return type for implemented method
		 */
		Type resultType();

		/**
		 * Returns number of parameters that reference method has.
		 *
		 * <p>This method exists for convenience, as it is always equal to the number of formal parameters in functional
		 * interface declaration.
		 *
		 * @return parameter count of referenced method
		 */
		int parametersCount();

		/**
		 * Returns actual type for parameter by its index.
		 *
		 * <p>This might be different from function type parameter because as it must have the same erasure, it can
		 * have different parameters, if its parameterized type, or it could be a type variable.
		 *
		 * @param index index of parameter to extract, counting from 0
		 * @return actual parameter type
		 */
		Type parameterType(int index);

		/**
		 * Returns annotation that was placed on parameter with specified index in implementation method.
		 *
		 * <p>WARNING: due to bug in JDK, this method can return empty set for lambda methods rather than actual
		 * annotations.
		 *
		 * <p>This method extracts formal parameter annotations that are placed on parameter of implementation method
		 * with specified index.
		 *
		 * <p>This method behaves differently when this reference was in "bound" mode, as first of the formal parameters
		 * in functional interface will be permanently bound to the specified instance. In this case calling this method
		 * with {@literal 0} will always produce empty set, as annotations cannot be applied on receiver parameter.
		 *
		 * @param index index of parameter to extract, counting from 0
		 * @return annotations on parameter type
		 */
		Set<Annotation> parameterAnnotations(int index);

		/**
		 * Extracts referenced method if the implementation was a method reference.
		 *
		 * <p>Returns reflection of method of this functional reference. This works for modes "static", "instance" and
		 * "bound".
		 *
		 * @return method that is referenced
		 * @throws IllegalStateException if mode of this functional reference is not any of method ones
		 */
		Method referencedMethod() throws IllegalStateException;

		/**
		 * Extracts referenced constructor if the implementation was a constructor reference.
		 *
		 * <p>Returns reflection of constructor of this functional reference. This works for modes "constructor" only.
		 *
		 * @return constructor that is referenced
		 * @throws IllegalStateException if this reference is not to a constructor
		 */
		Constructor<?> referencedConstructor() throws IllegalStateException;
	}

	/**
	 * Visitor Pattern interface for Functional reference.
	 *
	 * @param <T> type of return value from visitor
	 */
	interface Visitor<T> {
		/**
		 * Executes when mode of this reference is "static", i.e. this is a reference to static method.
		 *
		 * @param method method to which this functional references refers
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		T visitStatic(Method method);

		/**
		 * Executes when mode of this reference is "instance", i.e. this is a reference to instance method without
		 * bound instance.
		 *
		 * @param method method to which this functional references refers
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		T visitInstance(Method method);

		/**
		 * Executes when mode of this reference is "bound", i.e. this is a reference to instance method with
		 * bound instance present.
		 *
		 * @param method method to which this functional references refers
		 * @param boundInstance instance that was used to create reference
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		T visitBound(Method method, Object boundInstance);

		/**
		 * Executes when mode of this reference is "constructor", i.e. this is a reference to constructor.
		 *
		 * @param constructor constructor to which this functional references refers
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		T visitConstructor(Constructor<?> constructor);

		/**
		 * Executes when mode of this reference is "lambda".
		 *
		 * @param method synthetic method that this lambda created
		 * @param captures captures caught by this lambda
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		T visitLambda(Method method, List<Object> captures);
	}

	/**
	 * Visitor Pattern partial implementation that allows handling of groups of modes.
	 *
	 * <p>Calls {@link #fallback} on any unimplemented method. If {@link #visitMethod} is overridden, it is called when
	 * mode is "static", "instance" or "bound".
	 *
	 * @param <T> type of return value from visitor
	 */
	abstract class PartialVisitor<T> implements Visitor<T> {
		@Override
		public T visitStatic(Method method) {
			return visitMethod(method);
		}

		@Override
		public T visitInstance(Method method) {
			return visitMethod(method);
		}

		@Override
		public T visitBound(Method method, Object boundInstance) {
			return visitMethod(method);
		}

		@Override
		public T visitConstructor(Constructor<?> constructor) {
			return fallback();
		}

		@Override
		public T visitLambda(Method method, List<Object> captures) {
			return fallback();
		}

		/**
		 * Executes when mode of this reference is "static", "instance" or "bound".
		 *
		 * @param method method that this reference refers to
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		protected T visitMethod(Method method) {
			return fallback();
		}

		/**
		 * Executes when mode was not overridden.
		 *
		 * @return value passed out of {@link FunctionalReference.Introspection#visit}
		 */
		protected abstract T fallback();
	}
}
