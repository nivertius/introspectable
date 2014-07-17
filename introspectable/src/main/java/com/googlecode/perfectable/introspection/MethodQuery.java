package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public abstract class MethodQuery extends AbstractMemberQuery<Method, MethodQuery> {

	public static <X> MethodQuery of(Class<X> type) {
		checkNotNull(type);
		return new CompleteMethodQuery<>(type);
	}

	@Override
	public MethodQuery matching(Predicate<? super Method> filter) {
		checkNotNull(filter);
		return new FilteredMethodQuery(filter);
	}

	public MethodQuery parameters(Class<?>... parameterTypes) {
		checkNotNull(parameterTypes);
		Predicate<? super Method> filter = (candidate) -> Arrays.equals(parameterTypes, candidate.getParameterTypes());
		return matching(filter);
	}

	public MethodQuery returning(Class<?> type) {
		checkNotNull(type);
		Predicate<? super Method> filter = (candidate) -> type.equals(candidate.getReturnType());
		return matching(filter);
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
		public Iterator<Method> iterator() {
			return new AbstractCompleteMemberIterator<X, Method>(this.chain) {
				@Override
				protected Iterator<Method> extractMembers(Class<?> nextClass) {
					return Iterators.forArray(nextClass.getDeclaredMethods());
				}
			};
		}
	}

	private class FilteredMethodQuery extends MethodQuery {
		final Predicate<? super Method> filter;

		public FilteredMethodQuery(Predicate<? super Method> filter) {
			this.filter = filter;
		}

		@Override
		public Iterator<Method> iterator() {
			final Iterator<Method> parent = MethodQuery.this.iterator();
			return Iterators.filter(parent, this.filter);
		}
	}

}
