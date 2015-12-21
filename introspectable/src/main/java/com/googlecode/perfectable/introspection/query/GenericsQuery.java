package com.googlecode.perfectable.introspection.query;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.google.common.reflect.TypeToken;

public final class GenericsQuery<X> {
	private final Class<X> type;
	
	public static final <X> GenericsQuery<X> of(Class<X> type) {
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
		return new Resolver<>(parameter);
	}
	
	public static class Resolver<X> {
		private final Type resolved;
		
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
			final Class<? extends X> safeClass = (Class<? extends X>) instance.getClass();
			return resolve(safeClass);
		}
	}
	
}
