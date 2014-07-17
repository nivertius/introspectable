package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import com.google.common.base.Predicate;

public abstract class AbstractMemberQuery<M extends Member & AnnotatedElement, Q extends AbstractMemberQuery<M, ? extends Q>>
implements MemberQuery<M, Q> {
	
	@Override
	public abstract Q matching(Predicate<? super M> filter);
	
	@Override
	public final Q named(String name) {
		checkNotNull(name);
		Predicate<? super M> filter = (candidate) -> name.equals(candidate.getName());
		return matching(filter);
	}
	
}
