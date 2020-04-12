package org.perfectable.introspection;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

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

		Type resultType();

		int parametersCount();

		Type parameterType(int number);

		Set<Annotation> parameterAnnotations(int number);

		Method referencedMethod() throws IllegalStateException;

		Constructor<?> referencedConstructor() throws IllegalStateException;
	}

	interface Visitor<T> {
		T visitBound(Method method, Object boundInstance);

		T visitStatic(Method method);

		T visitInstance(Method method);

		T visitConstructor(Constructor<?> constructor);

		T visitLambda(Object... captures);
	}

	abstract class PartialVisitor<T> implements Visitor<T> {
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
			return fallback();
		}

		@Override
		public T visitLambda(Object... captures) {
			return fallback();
		}

		protected T visitMethod(Method method) {
			return fallback();
		}

		protected abstract T fallback();
	}
}
