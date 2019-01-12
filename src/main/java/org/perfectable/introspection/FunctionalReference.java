package org.perfectable.introspection; // SUPPRESS LENGTH // SUPPRESS GodClass

import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.ParametersFilter;
import org.perfectable.introspection.query.TypeFilter;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("serial")
public interface FunctionalReference extends Serializable {
	default Introspection introspect() {
		return Introspection.of(this);
	}

	interface Visitor<T> {
		T visitBound(Method method, Object boundInstance);

		T visitStatic(Method method);

		T visitInstance(Method method);

		T visitConstructor(Constructor<?> constructor);

		T visitLambda(Object... captures);
	}

	abstract class SingularVisitor<T> implements Visitor<T> {
		@Override
		public T visitBound(Method method, Object boundInstance) {
			return visitMethod(method);
		}

		@Override
		public T visitStatic(Method method) {
			return visitMethod(method);
		}

		@Override
		public T visitInstance(Method method) {
			return visitMethod(method);
		}

		@Override
		public T visitConstructor(Constructor<?> constructor) {
			return unexpected();
		}

		@Override
		public T visitLambda(Object... captures) {
			return unexpected();
		}

		protected T visitMethod(Method method) {
			return unexpected();
		}

		protected abstract T unexpected();
	}

	abstract class Introspection {
		static Introspection of(FunctionalReference marker) { // SUPPRESS CyclomaticComplexity Ncss
			Class<? extends FunctionalReference> markerClass = marker.getClass();
			Method writeReplace = MethodQuery.of(markerClass)
				.named("writeReplace")
				.returning(Object.class)
				.asAccessible()
				.unique();
			SerializedLambda serializedForm;
			try {
				serializedForm = (SerializedLambda) writeReplace.invoke(marker);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
			ClassLoader classLoader = markerClass.getClassLoader();
			switch (serializedForm.getImplMethodKind()) {
				case MethodHandleInfo.REF_invokeInterface:
				case MethodHandleInfo.REF_invokeVirtual:
					switch (serializedForm.getCapturedArgCount()) {
						case 0:
							return new OfInstanceMethod(serializedForm, classLoader);
						case 1:
							return new OfBoundMethod(serializedForm, classLoader);
						default:
							throw new AssertionError("Illegal number of captures for invokevirtual");
					}
				case MethodHandleInfo.REF_invokeStatic:
					return new OfStaticMethod(serializedForm, classLoader);
				case MethodHandleInfo.REF_invokeSpecial:
					return new OfLambda(serializedForm, classLoader);
				case MethodHandleInfo.REF_newInvokeSpecial:
					return new OfConstructorReference(serializedForm, classLoader);
				default:
					throw new AssertionError("Illegal MethodHandleInfo for lambda");
			}
		}

		public final Class<?> capturingType() {
			return capturingType;
		}

		public abstract Class<?> resultType();

		public abstract int parametersCount();

		public abstract Class<?> parameterType(int number);

		public abstract Type parameterGenericType(int number);

		public abstract AnnotatedElement parameterAnnotated(int number) throws IllegalArgumentException;

		@CanIgnoreReturnValue
		public abstract <T> T visit(Visitor<T> visitor);

		public abstract String referencedMethodName() throws IllegalStateException;

		public abstract Method referencedMethod() throws IllegalStateException;

		protected Constructor<?> referencedConstructor() {
			throw new IllegalStateException("Interface implementation is not a constructor reference");
		}

		protected final SerializedLambda serializedForm;
		protected final Class<?> implementationClass;
		protected final Class<?> capturingType;

		Introspection(SerializedLambda serializedForm, ClassLoader classLoader) {
			this.serializedForm = serializedForm;
			String declaringTypeName = formatClassName(serializedForm.getImplClass());
			String capturingClassName = formatClassName(serializedForm.getCapturingClass());
			this.capturingType = ClassLoaderIntrospection.of(classLoader).loadSafe(capturingClassName);
			this.implementationClass = ClassLoaderIntrospection.of(classLoader).loadSafe(declaringTypeName);
		}

		private static String formatClassName(String capturingClass) {
			return capturingClass.replaceAll("/", ".");
		}

		protected Class<?> getImplementationClass() {
			return implementationClass;
		}


		private abstract static class OfMethod extends Introspection {
			protected final Method implementationMethod;

			OfMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
				String methodName = serializedForm.getImplMethodName();
				MethodSignature signature = MethodSignature.read(serializedForm.getImplMethodSignature());
				implementationMethod = MethodQuery.of(implementationClass)
					.named(methodName)
					.parameters(ParametersFilter.typesExact(signature.runtimeParameterTypes(classLoader)))
					.returning(TypeFilter.exact(signature.runtimeResultType(classLoader)))
					.notOverridden()
					.asAccessible()
					.unique();
			}

			@Override
			public int parametersCount() {
				return implementationMethod.getParameterCount();
			}

			@Override
			public Class<?> parameterType(int number) {
				checkParameterNumber(number);
				return implementationMethod.getParameterTypes()[number];
			}

			@Override
			public Type parameterGenericType(int number) {
				checkParameterNumber(number);
				return implementationMethod.getGenericParameterTypes()[number];
			}

			@Override
			public AnnotatedElement parameterAnnotated(int number) {
				checkParameterNumber(number);
				return implementationMethod.getParameters()[number];
			}

			@Override
			public String referencedMethodName() throws IllegalStateException {
				return serializedForm.getImplMethodName();
			}

			@Override
			public Method referencedMethod() throws IllegalStateException {
				return implementationMethod;
			}

			@Override
			public final Class<?> resultType() {
				return implementationMethod.getReturnType();
			}

			void checkParameterNumber(int number) {
				checkArgument(number >= 0, "Parameter number must be non-negative"); // SUPPRESS MultipleStringLiterals
				checkArgument(number < parametersCount(), "Method has no parameter with index %s", number);
			}
		}

		private static final class OfInstanceMethod extends OfMethod {
			OfInstanceMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
			}

			@Override
			public <T> T visit(Visitor<T> visitor) {
				return visitor.visitInstance(implementationMethod);
			}

			@Override
			public int parametersCount() {
				return implementationMethod.getParameterCount() + 1;
			}

			@Override
			public Class<?> parameterType(int number) {
				checkParameterNumber(number);
				if (number == 0) {
					return implementationClass;
				}
				else {
					return implementationMethod.getParameterTypes()[number - 1];
				}
			}

			@Override
			public Type parameterGenericType(int number) {
				checkParameterNumber(number);
				if (number == 0) {
					return implementationClass;
				}
				else {
					return implementationMethod.getGenericParameterTypes()[number - 1];
				}
			}

			@Override
			public AnnotatedElement parameterAnnotated(int number) {
				checkParameterNumber(number);
				if (number == 0) {
					return implementationClass;
				}
				else {
					return implementationMethod.getParameters()[number - 1];
				}
			}
		}

		private static final class OfBoundMethod extends OfMethod {
			OfBoundMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
			}

			@Override
			public <T> T visit(Visitor<T> visitor) {
				Object boundInstance = serializedForm.getCapturedArg(0);
				return visitor.visitBound(implementationMethod, boundInstance);
			}
		}

		private static final class OfStaticMethod extends OfMethod {
			OfStaticMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
			}

			@Override
			public <T> T visit(Visitor<T> visitor) {
				return visitor.visitStatic(implementationMethod);
			}
		}

		private static final class OfLambda extends OfMethod {
			private final Object[] captures;

			OfLambda(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
				Object[] result = new Object[this.serializedForm.getCapturedArgCount()];
				for (int i = 0; i < result.length; i++) {
					Object capturedArg = this.serializedForm.getCapturedArg(i);
					result[i] = capturedArg;
				}
				this.captures = result;
			}

			@Override
			public <T> T visit(Visitor<T> visitor) {
				return visitor.visitLambda(captures);
			}

			// SUPPRESS MultipleStringLiterals
			@Override
			public Method referencedMethod() throws IllegalStateException {
				throw new IllegalStateException(
					"Interface implementation is not a method reference"); // SUPPRESS MultipleStringLiterals
			}

			@Override
			public String referencedMethodName() throws IllegalStateException {
				throw new IllegalStateException(
					"Interface implementation is not a method reference"); // SUPPRESS MultipleStringLiterals
			}
		}

		private static final class OfConstructorReference extends Introspection {
			private final Constructor<?> implementationConstructor;

			OfConstructorReference(SerializedLambda serializedForm, ClassLoader classLoader) {
				super(serializedForm, classLoader);
				MethodSignature signature = MethodSignature.read(this.serializedForm.getInstantiatedMethodType());
				implementationConstructor = ConstructorQuery.of(getImplementationClass())
					.parameters(signature.runtimeParameterTypes(classLoader))
					.asAccessible()
					.unique();
			}

			@Override
			public Method referencedMethod() throws IllegalStateException {
				throw new IllegalStateException(
					"Interface implementation is not a method reference"); // SUPPRESS MultipleStringLiterals
			}

			@Override
			public String referencedMethodName() throws IllegalStateException {
				throw new IllegalStateException(
					"Interface implementation is not a method reference"); // SUPPRESS MultipleStringLiterals
			}

			@Override
			protected Constructor<?> referencedConstructor() {
				return implementationConstructor;
			}

			@Override
			public <T> T visit(Visitor<T> visitor) {
				return visitor.visitConstructor(implementationConstructor);
			}

			@Override
			public Class<?> resultType() {
				return implementationConstructor.getDeclaringClass();
			}

			@Override
			public int parametersCount() {
				return implementationConstructor.getParameterCount();
			}

			@Override
			public Class<?> parameterType(int number) {
				checkParameterNumber(number);
				return implementationConstructor.getParameterTypes()[number];
			}

			@Override
			public Type parameterGenericType(int number) {
				checkParameterNumber(number);
				return implementationConstructor.getGenericParameterTypes()[number];
			}

			@Override
			public AnnotatedElement parameterAnnotated(int number) {
				checkParameterNumber(number);
				return implementationConstructor.getParameters()[number];
			}

			private void checkParameterNumber(int number) {
				checkArgument(number >= 0, "Parameter number must be non-negative"); // SUPPRESS MultipleStringLiterals
				checkArgument(number < parametersCount(), "Constructor has no parameter with index %s", number);
			}
		}
	}

}
