package com.googlecode.perfectable.introspection.query;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.googlecode.perfectable.introspection.InheritanceChain;

public abstract class MethodQuery extends MemberQuery<Method, MethodQuery> {

	public static <X> MethodQuery of(Class<X> type) {
		checkNotNull(type);
		return new CompleteMethodQuery<>(type);
	}

	@Override
	public MethodQuery named(String name) {
		checkNotNull(name);
		return new NamedMethodQuery(this, name);
	}

	@Override
	public MethodQuery matching(Predicate<? super Method> filter) {
		checkNotNull(filter);
		return new PredicatedMethodQuery(this, filter);
	}

	public MethodQuery parameters(Class<?>... parameterTypes) {
		checkNotNull(parameterTypes);
		return new ParametersMethodQuery(this, parameterTypes);
	}

	public MethodQuery returning(Class<?> type) {
		checkNotNull(type);
		return new ReturningMethodQuery(this, type);
	}

	public MethodQuery returningVoid() {
		return returning(Void.TYPE);
	}

	private static final class CompleteMethodQuery<X> extends MethodQuery {
		private final InheritanceChain<X> chain;

		public CompleteMethodQuery(Class<X> type) {
			this.chain = InheritanceChain.startingAt(type);
		}

		@Override
		public Stream<Method> stream() {
			return this.chain.stream()
					.flatMap(c -> Stream.of(c.getDeclaredMethods()));
		}
	}

	private static abstract class FilteredMethodQuery extends MethodQuery {
		private final MethodQuery parent;
		
		public FilteredMethodQuery(MethodQuery parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Method candidate);

		@Override
		public Stream<Method> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}
	
	private class PredicatedMethodQuery extends FilteredMethodQuery {
		private final Predicate<? super Method> filter;

		public PredicatedMethodQuery(MethodQuery parent, Predicate<? super Method> filter) {
			super(parent);
			this.filter = filter;
		}
		
		@Override
		protected boolean matches(Method candidate) {
			return this.filter.test(candidate);
		}
	}

	private class NamedMethodQuery extends FilteredMethodQuery {
		private final String name;

		public NamedMethodQuery(MethodQuery parent, String name) {
			super(parent);
			this.name = name;
		}
		
		@Override
		protected boolean matches(Method candidate) {
			return this.name.equals(candidate.getName());
		}
	}
	
	private class ParametersMethodQuery extends FilteredMethodQuery {
		private final Class<?>[] parameterTypes;

		public ParametersMethodQuery(MethodQuery parent, Class<?>[] parameterTypes) {
			super(parent);
			this.parameterTypes = parameterTypes;
		}
		
		@Override
		protected boolean matches(Method candidate) {
			Class<?>[] actualParameterTypes = candidate.getParameterTypes();
			if(this.parameterTypes.length != actualParameterTypes.length) {
				return false;
			}
			for(int i = 0; i < this.parameterTypes.length; i++) {
				Class<?> expectedParameterType = this.parameterTypes[i];
				Class<?> actualParameterType = actualParameterTypes[i];
				if(!expectedParameterType.isAssignableFrom(actualParameterType)) {
					return false;
				}
			}
			return true;
		}
	}
	
	private class ReturningMethodQuery extends FilteredMethodQuery {
		private final Class<?> returnType;

		public ReturningMethodQuery(MethodQuery parent, Class<?> returnType) {
			super(parent);
			this.returnType = returnType;
		}
		
		@Override
		protected boolean matches(Method candidate) {
			return this.returnType.equals(candidate.getReturnType());
		}
	}

}
