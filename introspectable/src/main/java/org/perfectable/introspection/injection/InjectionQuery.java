package org.perfectable.introspection.injection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.perfectable.introspection.injection.Injection.CompositeInjection;
import org.perfectable.introspection.query.AnnotationFilter;
import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.MemberQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.AnnotationFilter.SingleAnnotationFilter;

public abstract class InjectionQuery<T, I> {
	
	public static <X> InjectionQuery<X, Object> create() {
		return new CompleteInjectionQuery<>();
	}

	// MARK name is not intuitive
	public final Injection<T> push(I injected) {
		Stream.Builder<Injection<T>> builder = Stream.builder();
		fieldInjections(injected).forEach(builder::add);
		methodInjections(injected).forEach(builder::add);
		return builder.build().collect(Injection::createComposite, CompositeInjection::add,
				CompositeInjection::add);
	}
	
	private Stream<Injection<T>> fieldInjections(I injected) {
		FieldQuery fieldQuery = FieldQuery.of(injected.getClass());
		return limit(fieldQuery)
				.stream()
				.map(field -> Injection.create(field, injected));
	}
	
	private Stream<Injection<T>> methodInjections(I injected) {
		Function<Method, Injection<T>> i = method -> Injection.create(method, injected);
		return limit(MethodQuery.of(injected.getClass()))
				.stream()
				.map(i);
	}
	
	public InjectionQuery<T, I> named(String injectionName) {
		return new NamedInjectionQuery<>(this, injectionName);
	}
	
	public <X> InjectionQuery<T, X> typed(Class<X> injectionClass) {
		return new TypedInjectionQuery<>(this, injectionClass);
	}
	
	protected abstract <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limit(Q query);
	
	static final class CompleteInjectionQuery<T> extends InjectionQuery<T, Object> {
		
		@Override
		protected <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limit(Q query) {
			return query
					.annotatedWith(Inject.class)
					.excludingModifier(Modifier.STATIC)
					.excludingModifier(Modifier.FINAL);
		}
	}
	
	abstract static class FilteredInjectionQuery<T, I> extends InjectionQuery<T, I> {
		private final InjectionQuery<T, I> parent;
		
		public FilteredInjectionQuery(InjectionQuery<T, I> parent) {
			this.parent = parent;
		}
		
		protected abstract <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limitConcrete(
				Q query);
		
		@Override
		protected final <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limit(Q query) {
			Q parentLimited = this.parent.limit(query);
			return limitConcrete(parentLimited);
		}
	}
	
	static final class NamedInjectionQuery<T, I> extends FilteredInjectionQuery<T, I> {
		private final String injectionName;
		
		public NamedInjectionQuery(InjectionQuery<T, I> parent, String injectionName) {
			super(parent);
			this.injectionName = checkNotNull(injectionName);
		}
		
		@Override
		protected <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limitConcrete(Q query) {
			SingleAnnotationFilter<Named> filter = AnnotationFilter.of(Named.class)
					.andMatching(annotation -> this.injectionName.equals(annotation.value()));
			return query.annotatedWith(filter);
		}
	}
	
	static final class TypedInjectionQuery<T, I, J> extends InjectionQuery<T, J> {
		// this cannot inherit from FilteredInjectionQuery because of changed types
		private final InjectionQuery<T, I> parent;
		private final Class<J> injectionClass;
		
		public TypedInjectionQuery(InjectionQuery<T, I> parent, Class<J> injectionClass) {
			this.parent = parent;
			this.injectionClass = injectionClass;
		}
		
		@Override
		protected <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limit(Q query) {
			Q parentLimited = this.parent.limit(query);
			return parentLimited.typed(this.injectionClass);
		}
		
	}
}
