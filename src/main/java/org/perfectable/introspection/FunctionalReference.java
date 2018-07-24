package org.perfectable.introspection; // SUPPRESS GodClass

import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.ParametersFilter;
import org.perfectable.introspection.query.TypeFilter;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

@SuppressWarnings("serial")
public interface FunctionalReference extends Serializable {
	default Introspection introspect() {
		return Introspection.of(this);
	}

	interface Visitor<T> {
		T visitBound(Object boundInstance);

		T visitStatic();

		T visitInstance();

		T visitConstructor();

		T visitLambda(Object... captures);
	}

	abstract class SingularVisitor<T> implements Visitor<T> {
		@Override
		public T visitBound(Object boundInstance) {
			return unexpected();
		}

		@Override
		public T visitStatic() {
			return unexpected();
		}

		@Override
		public T visitInstance() {
			return unexpected();
		}

		@Override
		public T visitConstructor() {
			return unexpected();
		}

		@Override
		public T visitLambda(Object... captures) {
			return unexpected();
		}

		protected abstract T unexpected();
	}

	final class Introspection {
		public static Introspection of(FunctionalReference marker) {
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
			return new Introspection(serializedForm, markerClass.getClassLoader());
		}

		public Class<?> capturingType() {
			String capturingClass = serializedForm.getCapturingClass();
			String capturingClassName = formatClassName(capturingClass);
			return loadClass(capturingClassName);
		}

		public Class<?> resultType() {
			if (serializedForm.getImplMethodKind() == MethodHandleInfo.REF_newInvokeSpecial) {
				return referencedConstructor().getDeclaringClass();
			}
			else {
				return getImplementationMethod().getReturnType();
			}
		}

		public int parametersCount() {
			if (serializedForm.getImplMethodKind() == MethodHandleInfo.REF_newInvokeSpecial) {
				return referencedConstructor().getParameterCount();
			}
			int rawCount = getImplementationMethod().getParameterCount();
			if (serializedForm.getImplMethodKind() == MethodHandleInfo.REF_invokeVirtual
				&& serializedForm.getCapturedArgCount() == 0) {
				return rawCount + 1;
			}
			return rawCount;

		}

		public Class<?> parameterType(int number) {
			if (serializedForm.getImplMethodKind() == MethodHandleInfo.REF_invokeVirtual
				&& serializedForm.getCapturedArgCount() == 0) {
				if (number == 0) {
					return getImplementationClass();
				}
				else {
					return getImplementationMethod().getParameterTypes()[number - 1];
				}
			}
			return getImplementationMethod().getParameterTypes()[number];
		}

		public String referencedMethodName() throws IllegalStateException {
			assertMethodReference();
			return serializedForm.getImplMethodName();
		}

		public Method referencedMethod() throws IllegalStateException {
			assertMethodReference();
			return getImplementationMethod();
		}

		@SuppressWarnings("ReturnMissingNullable")
		public Constructor<?> referencedConstructor() {
			assertConstructorReference();
			if (implementationConstructor == null) {
				MethodSignature signature = MethodSignature.read(serializedForm.getInstantiatedMethodType());
				implementationConstructor = ConstructorQuery.of(getImplementationClass())
					.parameters(signature.runtimeParameterTypes(classLoader))
					.asAccessible()
					.unique();
			}
			return implementationConstructor;
		}

		public <T> T visit(Visitor<T> visitor) {
			switch (serializedForm.getImplMethodKind()) {
				case MethodHandleInfo.REF_invokeInterface:
				case MethodHandleInfo.REF_invokeVirtual:
					switch (serializedForm.getCapturedArgCount()) {
						case 0:
							return visitor.visitInstance();
						case 1:
							Object boundInstance = serializedForm.getCapturedArg(0);
							return visitor.visitBound(boundInstance);
						default:
							throw new AssertionError("Illegal number of captures for invokevirtual");
					}
				case MethodHandleInfo.REF_invokeStatic:
					return visitor.visitStatic();
				case MethodHandleInfo.REF_invokeSpecial:
					return visitor.visitLambda(captures());
				case MethodHandleInfo.REF_newInvokeSpecial:
					return visitor.visitConstructor();
				default:
					throw new AssertionError("Illegal MethodHandleInfo for lambda");
			}
		}

		private final SerializedLambda serializedForm;
		private final ClassLoader classLoader;

		@Nullable
		private Method implementationMethod;

		@Nullable
		private Class<?> implementationClass;

		@Nullable
		private Constructor<?> implementationConstructor;

		private Introspection(SerializedLambda serializedForm, ClassLoader classLoader) {
			this.classLoader = classLoader;
			this.serializedForm = serializedForm;
		}

		private static String formatClassName(String capturingClass) {
			return capturingClass.replaceAll("/", ".");
		}

		private Class<?> loadClass(String className) {
			return ClassLoaderIntrospection.of(classLoader)
				.loadSafe(className);
		}

		private void assertMethodReference() {
			int implMethodKind = serializedForm.getImplMethodKind();
			if (implMethodKind != MethodHandleInfo.REF_invokeVirtual
				&& implMethodKind != MethodHandleInfo.REF_invokeStatic
				&& implMethodKind != MethodHandleInfo.REF_invokeInterface) {
				throw new IllegalStateException("Interface implementation is not a method reference");
			}
		}

		private void assertConstructorReference() {
			int implMethodKind = serializedForm.getImplMethodKind();
			if (implMethodKind != MethodHandleInfo.REF_newInvokeSpecial) {
				throw new IllegalStateException("Interface implementation is not a constructor reference");
			}
		}

		@SuppressWarnings("ReturnMissingNullable")
		private Class<?> getImplementationClass() {
			if (implementationClass == null) {
				String declaringTypeName = formatClassName(serializedForm.getImplClass());
				implementationClass = loadClass(declaringTypeName);
			}
			return implementationClass;
		}

		@SuppressWarnings("ReturnMissingNullable")
		private Method getImplementationMethod() {
			if (implementationMethod == null) {
				String methodName = serializedForm.getImplMethodName();
				MethodSignature signature = MethodSignature.read(serializedForm.getImplMethodSignature());
				implementationMethod = MethodQuery.of(getImplementationClass())
					.named(methodName)
					.parameters(ParametersFilter.typesExact(signature.runtimeParameterTypes(classLoader)))
					.returning(TypeFilter.exact(signature.runtimeResultType(classLoader)))
					.notOverridden()
					.asAccessible()
					.unique();
			}
			return implementationMethod;
		}

		private Object[] captures() {
			Object[] result = new Object[serializedForm.getCapturedArgCount()];
			for (int i = 0; i < result.length; i++) {
				Object capturedArg = serializedForm.getCapturedArg(i);
				result[i] = capturedArg;
			}
			return result;
		}

	}
}
