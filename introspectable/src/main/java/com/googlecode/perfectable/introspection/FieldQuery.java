package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public abstract class FieldQuery extends AbstractMemberQuery<Field, FieldQuery> implements
MemberQuery<Field, FieldQuery> {
	
	public static <X> FieldQuery of(Class<X> type) {
		checkNotNull(type);
		return new CompleteFieldQuery<>(type);
	}
	
	@Override
	public FieldQuery matching(Predicate<? super Field> filter) {
		checkNotNull(filter);
		return new FilteredFieldQuery(filter);
	}
	
	public FieldQuery ofType(Class<?> type) {
		checkNotNull(type);
		Predicate<? super Field> filter = (candidate) -> type.isAssignableFrom(candidate.getType());
		return matching(filter);
	}
	
	private static final class CompleteFieldQuery<X> extends FieldQuery {
		private final InheritanceChain<X> chain;

		public CompleteFieldQuery(Class<X> type) {
			this.chain = InheritanceChain.startingAt(type);
		}

		@Override
		public Iterator<Field> iterator() {
			return new AbstractCompleteMemberIterator<X, Field>(this.chain) {
				@Override
				protected Iterator<Field> extractMembers(Class<?> nextClass) {
					return Iterators.forArray(nextClass.getDeclaredFields());
				}
				
			};
		}
	}
	
	private class FilteredFieldQuery extends FieldQuery {
		final Predicate<? super Field> filter;
		
		public FilteredFieldQuery(Predicate<? super Field> filter) {
			this.filter = filter;
		}
		
		@Override
		public Iterator<Field> iterator() {
			final Iterator<Field> parent = FieldQuery.this.iterator();
			return Iterators.filter(parent, this.filter);
		}
	}
	
	FieldQuery() {
		// MARK
	}

}
