package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;

public abstract class MemberQuery<M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>>
		implements Iterable<M> {
	
	public abstract Q named(String name);
	
	public abstract Q typed(Class<?> type);
	
	public abstract Q matching(Predicate<? super M> filter);
	
	public Q annotatedWith(Class<? extends Annotation> annotationClass) {
		return annotatedWith(AnnotationFilter.of(annotationClass));
	}
	
	public abstract Q annotatedWith(AnnotationFilter annotationFilter);
	
	public abstract Q excludingModifier(int excludedModifier);
	
	public abstract Stream<M> stream();
	
	@Override
	public Iterator<M> iterator() {
		return stream().iterator();
	}
	
	public final M single() {
		return Iterators.getOnlyElement(iterator());
	}
	
	public final Optional<M> option() {
		Iterator<M> iterator = iterator();
		if(iterator.hasNext()) {
			return Optional.of(iterator.next());
		}
		return Optional.empty();
	}
	
	MemberQuery() {
		// package extension only
	}
}
