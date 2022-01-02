package org.perfectable.introspection;

import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.ParametersFilter;
import org.perfectable.introspection.query.TypeFilter;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kotlin.jvm.internal.CallableReference;
import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.KFunctionImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.perfectable.introspection.Introspections.introspect;

abstract class FunctionalReferenceIntrospection implements FunctionalReference.Introspection {
	private static final boolean KOTLIN_REFLECT_AVAILABLE = determineKotlinAvailability();

	static FunctionalReferenceIntrospection of(FunctionalReference marker) {
		if (KOTLIN_REFLECT_AVAILABLE) {
			if (marker instanceof CallableReference) {
				return ofKotlinCallable((CallableReference) marker);
			}
			try {
				Field functionField = marker.getClass().getDeclaredField("function");
				PrivilegedActions.markAccessible(functionField);
				Object function = functionField.get(marker);
				if (function != null) {
					return ofKotlinCallable((CallableReference) function);
				}
			}
			catch (NoSuchFieldException | IllegalAccessException e) {
				// pass
			}
 		}
		Class<? extends FunctionalReference> markerClass = marker.getClass();
		Optional<Method> writeReplaceOption = MethodQuery.of(markerClass)
			.named("writeReplace")
			.returning(Object.class)
			.asAccessible()
			.option();
		if (writeReplaceOption.isPresent()) {
			Method writeReplace = writeReplaceOption.get();
			ClassLoaderIntrospection classLoader = introspect(markerClass).classLoader();
			return ofNativeImplementation(marker, writeReplace, classLoader);
		}
		else {
			// SUPPRESS NEXT MultipleStringLiterals
			throw new IllegalArgumentException("Unsupported functional interface implementation " + marker);
		}
	}

	static FunctionalReferenceIntrospection ofKotlin(Object function) {
		if (function instanceof CallableReference) {
			return ofKotlinCallable((CallableReference) function);
		}
		// SUPPRESS NEXT MultipleStringLiterals
		throw new IllegalArgumentException("Unsupported functional interface implementation " + function);
	}

	private static FunctionalReferenceIntrospection ofKotlinCallable(CallableReference function) {
		KClassImpl<?> owner = (KClassImpl<?>) function.getOwner();
		String methodName = function.getName();
		String signature = function.getSignature();
		KFunctionImpl computed = (KFunctionImpl) function.compute();
		Class<?> declaringClass = computed.getContainer().getJClass();
		@SuppressWarnings("cast.unsafe")
		Method implementationMethod = (@NonNull Method) owner.findMethodBySignature(methodName, signature);
		checkArgument(implementationMethod != null, "Method with name %s and signature %s not found in %s",
			methodName, signature, owner);
		Object receiver = function.getBoundReceiver();
		if (receiver == CallableReference.NO_RECEIVER) {
			if (Modifier.isStatic(implementationMethod.getModifiers())) {
				return new OfStaticMethod(implementationMethod, declaringClass, declaringClass);
			}
			else {
				return new OfInstanceMethod(implementationMethod, declaringClass, declaringClass);
			}
		}
		else {
			return new OfBoundMethod(receiver, implementationMethod, declaringClass, declaringClass);
		}
	}

	private static FunctionalReferenceIntrospection ofNativeImplementation(
			FunctionalReference marker, Method writeReplace, ClassLoaderIntrospection classLoader) {
		SerializedLambda serializedForm = extractSerializedLambda(marker, writeReplace);
		switch (serializedForm.getImplMethodKind()) {
			case MethodHandleInfo.REF_invokeInterface:
			case MethodHandleInfo.REF_invokeVirtual:
				switch (serializedForm.getCapturedArgCount()) {
					case 0:
						return OfInstanceMethod.fromSerializedLambda(serializedForm, classLoader);
					case 1:
						return OfBoundMethod.fromSerializedLambda(serializedForm, classLoader);
					default:
						throw new AssertionError("Illegal number of captures for invokevirtual");
				}
			case MethodHandleInfo.REF_invokeStatic:
				return OfStaticMethod.fromSerializedLambda(serializedForm, classLoader);
			case MethodHandleInfo.REF_invokeSpecial:
				return OfLambda.fromSerializedLambda(serializedForm, classLoader);
			case MethodHandleInfo.REF_newInvokeSpecial:
				return OfConstructorReference.fromSerializedLambda(serializedForm, classLoader);
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

	protected final Class<?> implementationClass;
	protected final Class<?> capturingType;

	FunctionalReferenceIntrospection(Class<?> implementationClass, Class<?> capturingType) {
		this.implementationClass = implementationClass;
		this.capturingType = capturingType;
	}

	private static String formatClassName(String capturingClass) {
		return capturingClass.replaceAll("/", ".");
	}

	@SuppressWarnings("cast.unsafe")
	private static SerializedLambda extractSerializedLambda(FunctionalReference marker, Method writeReplace) {
		SerializedLambda serializedForm;
		try {
			serializedForm = (@NonNull SerializedLambda) writeReplace.invoke(marker);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new LinkageError(e.getMessage(), e);
		}
		return serializedForm;
	}

	protected void checkParameterNumber(int number) {
		checkArgument(number >= 0, "Parameter number must be non-negative");
		checkArgument(number < parametersCount(), "Executable has no parameter with index %s", number);
	}

	static Class<?> findImplementationClass(SerializedLambda serializedForm, ClassLoaderIntrospection classLoader) {
		return classLoader.loadSafe(formatClassName(serializedForm.getImplClass()));
	}

	static Class<?> findCapturingType(SerializedLambda serializedForm, ClassLoaderIntrospection classLoader) {
		return classLoader.loadSafe(formatClassName(serializedForm.getCapturingClass()));
	}

	static Method findImplementationMethod(SerializedLambda serializedForm,
										   ClassLoaderIntrospection classLoader, Class<?> implementationClass) {
		String methodName = serializedForm.getImplMethodName();
		String signatureString = serializedForm.getImplMethodSignature();
		return findBySignature(classLoader, implementationClass, methodName, signatureString);
	}

	private static Method findBySignature(ClassLoaderIntrospection classLoader, Class<?> implementationClass, String methodName, String signatureString) {
		MethodSignature signature = MethodSignature.read(signatureString);
		String signatureName = signature.name();
		String actualName = signatureName.isEmpty() ? methodName : signatureName;
		return MethodQuery.of(implementationClass)
			.named(actualName)
			.parameters(ParametersFilter.typesExact(signature.runtimeParameterTypes(classLoader)))
			.returning(TypeFilter.exact(signature.runtimeResultType(classLoader)))
			.notOverridden()
			.asAccessible()
			.unique();
	}

	private static boolean determineKotlinAvailability() {
		try {
			Class.forName("kotlin.jvm.internal.CallableReference");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	private abstract static class OfMethod extends FunctionalReferenceIntrospection {
		protected final Method implementationMethod;

		OfMethod(Method implementationMethod, Class<?> implementationClass, Class<?> captureType) {
			super(implementationClass, captureType);
			this.implementationMethod = implementationMethod;
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


	}

	private static final class OfInstanceMethod extends OfMethod {
		static OfInstanceMethod fromSerializedLambda(SerializedLambda serializedForm,
													 ClassLoaderIntrospection classLoader) {
			Class<?> implementationClass = findImplementationClass(serializedForm, classLoader);
			Class<?> capturingType = findCapturingType(serializedForm, classLoader);
			Method implementationMethod = findImplementationMethod(serializedForm, classLoader, implementationClass);
			return new OfInstanceMethod(implementationMethod, implementationClass, capturingType);
		}

		OfInstanceMethod(Method implementationMethod, Class<?> implementationClass, Class<?> capturingType) {
			super(implementationMethod, implementationClass, capturingType);
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
		private final Object boundInstance;

		static OfBoundMethod fromSerializedLambda(SerializedLambda serializedForm,
												  ClassLoaderIntrospection classLoader) {
			Object boundInstance = serializedForm.getCapturedArg(0);
			Class<?> implementationClass = findImplementationClass(serializedForm, classLoader);
			Class<?> capturingType = findCapturingType(serializedForm, classLoader);
			Method implementationMethod = findImplementationMethod(serializedForm, classLoader, implementationClass);
			return new OfBoundMethod(boundInstance, implementationMethod, implementationClass, capturingType);
		}

		OfBoundMethod(Object boundInstance, Method implementationMethod, Class<?> implementationClass,
							 Class<?> capturingType) {
			super(implementationMethod, implementationClass, capturingType);
			this.boundInstance = boundInstance;
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitBound(implementationMethod, boundInstance);
		}
	}

	private static final class OfStaticMethod extends OfMethod {
		static FunctionalReferenceIntrospection fromSerializedLambda(
			SerializedLambda serializedForm, ClassLoaderIntrospection classLoader) {
			Class<?> implementationClass = findImplementationClass(serializedForm, classLoader);
			Class<?> capturingType = findCapturingType(serializedForm, classLoader);
			Method implementationMethod = findImplementationMethod(serializedForm, classLoader, implementationClass);
			return new OfStaticMethod(implementationMethod, implementationClass, capturingType);
		}

		OfStaticMethod(Method implementationMethod, Class<?> implementationClass, Class<?> capturingType) {
			super(implementationMethod, implementationClass, capturingType);
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitStatic(implementationMethod);
		}
	}

	private static final class OfLambda extends OfMethod {
		private final ImmutableList<Object> captures;

		static FunctionalReferenceIntrospection fromSerializedLambda(SerializedLambda serializedForm,
																	 ClassLoaderIntrospection classLoader) {
			Class<?> implementationClass = findImplementationClass(serializedForm, classLoader);
			Class<?> capturingType = findCapturingType(serializedForm, classLoader);
			Method implementationMethod = findImplementationMethod(serializedForm, classLoader, implementationClass);
			ImmutableList.Builder<Object> capturesBuilder = ImmutableList.builder();
			for (int i = 0; i < serializedForm.getCapturedArgCount(); i++) {
				Object capturedArg = serializedForm.getCapturedArg(i);
				capturesBuilder.add(capturedArg);
			}
			ImmutableList<Object> captures = capturesBuilder.build();
			return new OfLambda(captures, implementationMethod, implementationClass, capturingType);
		}

		private OfLambda(ImmutableList<Object> captures, Method implementationMethod, Class<?> implementationClass,
						 Class<?> capturingType) {
			super(implementationMethod, implementationClass, capturingType);
			this.captures = captures;
		}

		@Override
		public <T> T visit(FunctionalReference.Visitor<T> visitor) {
			return visitor.visitLambda(implementationMethod, captures);
		}

		@SuppressWarnings("MultipleStringLiterals")
		@Override
		public Method referencedMethod() throws IllegalStateException {
			throw new IllegalStateException(
				"Interface implementation is not a method reference");
		}
	}

	private static final class OfConstructorReference extends FunctionalReferenceIntrospection {
		private final Constructor<?> implementationConstructor;

		static FunctionalReferenceIntrospection fromSerializedLambda(SerializedLambda serializedForm,
																			ClassLoaderIntrospection classLoader) {
			MethodSignature signature = MethodSignature.read(serializedForm.getInstantiatedMethodType());
			Class<?> implementationClass = findImplementationClass(serializedForm, classLoader);
			Class<?> capturingType = findCapturingType(serializedForm, classLoader);
			Constructor<?> implementationConstructor = ConstructorQuery.of(implementationClass)
				.parameters(signature.runtimeParameterTypes(classLoader))
				.asAccessible()
				.unique();
			return new OfConstructorReference(implementationConstructor, implementationClass, capturingType);
		}

		private OfConstructorReference(Constructor<?> implementationConstructor,
									   Class<?> implementationClass, Class<?> capturingType) {
			super(implementationClass, capturingType);
			this.implementationConstructor = implementationConstructor;
		}

		@SuppressWarnings("MultipleStringLiterals")
		@Override
		public Method referencedMethod() throws IllegalStateException {
			throw new IllegalStateException(
				"Interface implementation is not a method reference");
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
	}
}
