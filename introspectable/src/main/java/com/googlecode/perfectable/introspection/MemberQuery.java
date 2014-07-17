package com.googlecode.perfectable.introspection;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.Optional;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public interface MemberQuery<M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>>
		extends Iterable<M> {

	Q named(String name);
	
	Q matching(Predicate<? super M> filter);
	
	default M single() {
		return Iterators.getOnlyElement(iterator());
	}
	
	default Optional<M> option() {
		final Iterator<M> iterator = iterator();
		if(iterator.hasNext()) {
			return Optional.of(iterator.next());
		}
		return Optional.empty();
	}
	
}
