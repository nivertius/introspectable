package org.perfectable.introspection.query;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.checkArgument;

public final class GenericsQuery<X> {
	private final Class<X> type;

	public static <X> GenericsQuery<X> of(Class<X> type) {
		return new GenericsQuery<>(type);
	}

	public GenericsQuery(Class<X> type) {
		this.type = type;
	}

	public Resolver<X> parameter(int number) {
		checkArgument(number >= 0);
		TypeVariable<Class<X>>[] typeParameters = this.type.getTypeParameters();
		checkArgument(number < typeParameters.length);
		TypeVariable<Class<X>> parameter = typeParameters[number];
		return Resolver.of(parameter);
	}

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
	}

}
