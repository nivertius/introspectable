package org.perfectable.introspection;

import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.MethodQuery;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

	final class Introspection {
		public static Introspection of(FunctionalReference marker) {
			Method writeReplace = MethodQuery.of(marker.getClass())
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
			return new Introspection(serializedForm);
		}

		public Class<?> capturingType() {
			String capturingClass = serializedForm.getCapturingClass();
			String capturingClassName = formatClassName(capturingClass);
			ClassLoader loader = getClass().getClassLoader();
			return loadClass(loader, capturingClassName);
		}

		public String referencedMethodName() throws IllegalStateException {
			int implMethodKind = serializedForm.getImplMethodKind();
			if (implMethodKind != MethodHandleInfo.REF_invokeVirtual
				&& implMethodKind != MethodHandleInfo.REF_invokeStatic
				&& implMethodKind != MethodHandleInfo.REF_invokeInterface) {
				throw new IllegalStateException("Interface implementation is not a method reference");
			}
			return serializedForm.getImplMethodName();
		}

		public Method referencedMethod() throws IllegalStateException {
			String methodName = referencedMethodName();
			ClassLoader loader = getClass().getClassLoader();
			Class<?> implementationClass = getImplementationClass(loader);
			MethodSignature signature = MethodSignature.read(serializedForm.getImplMethodSignature());
			return MethodQuery.of(implementationClass)
				.named(methodName)
				.parameters(signature.runtimeParameterTypes(loader))
				.returning(signature.runtimeResultType(loader))
				.notOverridden()
				.asAccessible()
				.unique();
		}

		public Constructor<?> referencedConstructor() {
			int implMethodKind = serializedForm.getImplMethodKind();
			if (implMethodKind != MethodHandleInfo.REF_newInvokeSpecial) {
				throw new IllegalStateException("Interface implementation is not a constructor reference");
			}
			ClassLoader loader = getClass().getClassLoader();
			Class<?> implementationClass = getImplementationClass(loader);
			MethodSignature signature = MethodSignature.read(serializedForm.getInstantiatedMethodType());
			return ConstructorQuery.of(implementationClass)
				.parameters(signature.runtimeParameterTypes(loader))
				.asAccessible()
				.unique();
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

		private Introspection(SerializedLambda serializedForm) {
			this.serializedForm = serializedForm;
		}

		private static String formatClassName(String capturingClass) {
			return capturingClass.replaceAll("/", ".");
		}

		private static Class<?> loadClass(ClassLoader loader, String className) {
			return ClassLoaderIntrospection.of(loader)
				.loadSafe(className);
		}

		private Class<?> getImplementationClass(ClassLoader loader) {
			String declaringTypeName = formatClassName(serializedForm.getImplClass());
			return loadClass(loader, declaringTypeName);
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
