package org.perfectable.introspection; // SUPPRESS LENGTH

import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.ParametersFilter;
import org.perfectable.introspection.query.TypeFilter;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkArgument;

abstract class FunctionalReferenceIntrospection implements FunctionalReference.Introspection {
	static FunctionalReferenceIntrospection of(FunctionalReference marker) {
		Class<? extends FunctionalReference> markerClass = marker.getClass();
		Optional<Method> writeReplaceOption = MethodQuery.of(markerClass)
			.named("writeReplace")
			.returning(Object.class)
			.asAccessible()
			.option();
		if (writeReplaceOption.isPresent()) {
			Method writeReplace = writeReplaceOption.get();
			ClassLoader classLoader = markerClass.getClassLoader();
			return ofNativeImplementation(marker, writeReplace, classLoader);
		}
		else {
			throw new IllegalArgumentException("Unsupported functional interface implementation " + marker);
		}
	}

	private static FunctionalReferenceIntrospection ofNativeImplementation(// SUPPRESS CyclomaticComplexity
			FunctionalReference marker, Method writeReplace, ClassLoader classLoader) {
		SerializedLambda serializedForm;
		try {
			serializedForm = (SerializedLambda) writeReplace.invoke(marker);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new AssertionError(e);
		}
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

	@Override
	public final Class<?> capturingType() {
		return capturingType;
	}

	@Override
	public Constructor<?> referencedConstructor() {
		throw new IllegalStateException("Interface implementation is not a constructor reference");
	}

	protected final SerializedLambda serializedForm;
	protected final Class<?> implementationClass;
	protected final Class<?> capturingType; // SUPPRESS AvoidFieldNameMatchingMethodName

	FunctionalReferenceIntrospection(SerializedLambda serializedForm, ClassLoader classLoader) {
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

	private abstract static class OfMethod extends FunctionalReferenceIntrospection {
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
		public Type parameterType(int index) {
			checkParameterNumber(index);
			return implementationMethod.getGenericParameterTypes()[index];
		}

		@Override
		public Set<Annotation> parameterAnnotations(int index) {
			checkParameterNumber(index);
			Annotation[] annotations = implementationMethod.getParameterAnnotations()[index];
			return ImmutableSet.copyOf(annotations);
		}

		@Override
		public Method referencedMethod() throws IllegalStateException {
			return implementationMethod;
		}

		@Override
		public final Type resultType() {
			return implementationMethod.getGenericReturnType();
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
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitInstance(implementationMethod);
		}

		@Override
		public int parametersCount() {
			return implementationMethod.getParameterCount() + 1;
		}

		@Override
		public Type parameterType(int index) {
			checkParameterNumber(index);
			if (index == 0) {
				return implementationClass;
			}
			else {
				return implementationMethod.getGenericParameterTypes()[index - 1];
			}
		}

		@Override
		public Set<Annotation> parameterAnnotations(int index) {
			checkParameterNumber(index);
			if (index == 0) {
				return ImmutableSet.of();
			}
			else {
				Annotation[] parameterAnnotations = implementationMethod.getParameterAnnotations()[index - 1];
				return ImmutableSet.copyOf(parameterAnnotations);
			}
		}
	}

	private static final class OfBoundMethod extends OfMethod {
		OfBoundMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
			super(serializedForm, classLoader);
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			Object boundInstance = serializedForm.getCapturedArg(0);
			return visitor.visitBound(implementationMethod, boundInstance);
		}
	}

	private static final class OfStaticMethod extends OfMethod {
		OfStaticMethod(SerializedLambda serializedForm, ClassLoader classLoader) {
			super(serializedForm, classLoader);
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitStatic(implementationMethod);
		}
	}

	private static final class OfLambda extends OfMethod {
		private final ImmutableList<Object> captures;

		OfLambda(SerializedLambda serializedForm, ClassLoader classLoader) {
			super(serializedForm, classLoader);
			ImmutableList.Builder<Object> result = ImmutableList.builder();
			for (int i = 0; i < this.serializedForm.getCapturedArgCount(); i++) {
				Object capturedArg = this.serializedForm.getCapturedArg(i);
				result.add(capturedArg);
			}
			this.captures = result.build();
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitLambda(implementationMethod, captures);
		}

		// SUPPRESS MultipleStringLiterals
		@Override
		public Method referencedMethod() throws IllegalStateException {
			throw new IllegalStateException(
				"Interface implementation is not a method reference"); // SUPPRESS MultipleStringLiterals
		}
	}

	private static final class OfConstructorReference extends FunctionalReferenceIntrospection {
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
		public Constructor<?> referencedConstructor() {
			return implementationConstructor;
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
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
		public Type parameterType(int index) {
			checkParameterNumber(index);
			return implementationConstructor.getGenericParameterTypes()[index];
		}

		@Override
		public Set<Annotation> parameterAnnotations(int index) {
			checkParameterNumber(index);
			Annotation[] annotations = implementationConstructor.getParameterAnnotations()[index];
			return ImmutableSet.copyOf(annotations);
		}

		private void checkParameterNumber(int number) {
			checkArgument(number >= 0, "Parameter number must be non-negative"); // SUPPRESS MultipleStringLiterals
			checkArgument(number < parametersCount(), "Constructor has no parameter with index %s", number);
		}
	}
}
