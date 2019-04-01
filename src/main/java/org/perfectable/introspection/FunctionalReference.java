package org.perfectable.introspection;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

@SuppressWarnings("serial")
public interface FunctionalReference extends Serializable {
	default Introspection introspect() {
		return FunctionalReferenceIntrospection.of(this);
	}

	interface Introspection {
		@CanIgnoreReturnValue
		<T> T visit(Visitor<T> visitor);

		Class<?> capturingType();

		Class<?> resultType();

		int parametersCount();

		Class<?> parameterType(int number);

		Type genericParameterType(int number);

		AnnotatedElement annotatedParameter(int number) throws IllegalArgumentException;

		String referencedMethodName() throws IllegalStateException;

		Method referencedMethod() throws IllegalStateException;

		Constructor<?> referencedConstructor();
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
}
