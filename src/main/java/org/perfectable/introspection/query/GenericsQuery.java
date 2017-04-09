package org.perfectable.introspection.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class GenericsQuery<X> {

	public static <X> GenericsQuery<X> of(Class<X> type) {
		return new OfClass<>(type);
	}

	public static GenericsQuery<Object> of(Method method) {
		return new OfMethod(method);
	}

	public static GenericsQuery<Object> of(Parameter parameter) {
		return new OfParameter(parameter);
	}

	public static GenericsQuery<Object> of(Field field) {
		return new OfField(field);
	}

	public abstract Resolver<X> parameter(int number);

	public static final class Resolver<X> {
		private final Type resolved;

		static <X> Resolver<X> of(Type parameter) {
			return new Resolver<>(parameter);
		}

		private Resolver(Type resolved) {
			this.resolved = resolved;
		}

		public Class<?> resolve(Class<? extends X> targetClass) {
			TypeToken<? extends X> token = TypeToken.of(targetClass);
			TypeToken<?> resolvedToken = token.resolveType(this.resolved);
			return resolvedToken.getRawType();
		}

		public Class<?> resolve(X instance) {
			@SuppressWarnings("unchecked")
			Class<? extends X> safeClass = (Class<? extends X>) instance.getClass();
			return resolve(safeClass);
		}

		static Resolver<Object> createFromParameterizedType(ParameterizedType parameterizedType, int number) {
			checkArgument(number >= 0);
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			checkArgument(number < typeArguments.length);
			return of(typeArguments[number]);
		}

	}

	public static final class OfClass<X> extends GenericsQuery<X> {
		private final Class<X> type;

		OfClass(Class<X> type) {
			this.type = type;
		}

		@Override
		public Resolver<X> parameter(int number) {
			TypeVariable<Class<X>>[] typeParameters = type.getTypeParameters();
			checkArgument(number >= 0);
			checkArgument(number < typeParameters.length);
			TypeVariable<Class<X>> parameter = typeParameters[number];
			return Resolver.of(parameter);
		}
	}

	public static final class OfParameter extends GenericsQuery<Object> {
		private final Parameter parameter; // SUPPRESS AvoidFieldNameMatchingMethodName

		OfParameter(Parameter parameter) {
			this.parameter = parameter;
		}

		@Override
		public Resolver<Object> parameter(int number) {
			Type parameterizedType = parameter.getParameterizedType();
			checkArgument(parameterizedType instanceof ParameterizedType);
			return Resolver.createFromParameterizedType((ParameterizedType) parameterizedType, number);
		}
	}

	public static final class OfField extends GenericsQuery<Object> {
		private final Field field;

		OfField(Field field) {
			this.field = field;
		}

		@Override
		public Resolver<Object> parameter(int number) {
			Type parameterizedType = field.getGenericType();
			checkArgument(parameterizedType instanceof ParameterizedType);
			return Resolver.createFromParameterizedType((ParameterizedType) parameterizedType, number);
		}
	}

	public static final class OfMethod extends GenericsQuery<Object> {
		private final Method method;

		OfMethod(Method method) {
			this.method = method;
		}

		@Override
		public Resolver<Object> parameter(int number) {
			TypeVariable<?>[] typeParameters = method.getTypeParameters();
			checkArgument(number >= 0);
			checkArgument(number < typeParameters.length);
			TypeVariable<?> parameter = typeParameters[number];
			return Resolver.of(parameter);
		}
	}
}
