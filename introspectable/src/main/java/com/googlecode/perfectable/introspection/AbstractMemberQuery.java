package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

public abstract class AbstractMemberQuery<M extends Member & AnnotatedElement, Q extends AbstractMemberQuery<M, ? extends Q>>
implements MemberQuery<M, Q> {
	
	protected static abstract class AbstractCompleteMemberIterator<X, M> extends AbstractIterator<M> {
		Iterator<Class<? super X>> chainIterator;
		Iterator<M> current = Iterators.emptyIterator();
		
		public AbstractCompleteMemberIterator(InheritanceChain<X> chain) {
			this.chainIterator = chain.iterator();
		}

		@Override
		protected final M computeNext() {
			while(!this.current.hasNext()) {
				if(!this.chainIterator.hasNext()) {
					return endOfData();
				}
				final Class<?> nextClass = this.chainIterator.next();
				this.current = extractMembers(nextClass);
			}
			return this.current.next();
		}
		
		protected abstract Iterator<M> extractMembers(final Class<?> nextClass);
	}
	
	@Override
	public abstract Q matching(Predicate<? super M> filter);
	
	@Override
	public final Q named(String name) {
		checkNotNull(name);
		Predicate<? super M> filter = (candidate) -> name.equals(candidate.getName());
		return matching(filter);
	}
	
}
