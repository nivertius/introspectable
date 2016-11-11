package org.perfectable.introspection.injection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.perfectable.introspection.query.AnnotationFilter;
import org.perfectable.introspection.query.AnnotationFilter.SingleAnnotationFilter;
import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.MemberQuery;
import org.perfectable.introspection.query.MethodQuery;

public abstract class InjectionQuery<T, I> {
	
	public static <X> InjectionQuery<X, Object> create(Class<X> targetClass) {
		return new CompleteInjectionQuery<>(targetClass);
	}

	// MARK name is not intuitive
	public final Injection<T> push(I injected) {
		Stream.Builder<Injection<T>> builder = Stream.builder();
		fieldInjections(injected).forEach(builder::add);
		methodInjections(injected).forEach(builder::add);
		return builder.build().collect(CompositeInjection::create, Injection::andThen, Injection::andThen);
	}
	
	private Stream<Injection<T>> fieldInjections(I injected) {
		return createFieldQuery()
				.stream()
				.map(field -> Injection.create(field, injected));
	}
	
	private Stream<Injection<T>> methodInjections(I injected) {
		return createMethodQuery()
				.stream()
				.map(method -> Injection.create(method, injected));
	}
	
	public InjectionQuery<T, I> named(String injectionName) {
		return new NamedInjectionQuery<>(this, injectionName);
	}
	
	public <X> InjectionQuery<T, X> typed(Class<X> injectionClass) {
		return new TypedInjectionQuery<>(this, injectionClass);
	}
	
	protected abstract MethodQuery createMethodQuery();
	protected abstract FieldQuery createFieldQuery();

	static final class CompleteInjectionQuery<T> extends InjectionQuery<T, Object> {

		private final Class<T> targetClass;

		CompleteInjectionQuery(Class<T> targetClass) {
			this.targetClass = targetClass;
		}

		@Override
		protected FieldQuery createFieldQuery() {
			FieldQuery initial = FieldQuery.of(targetClass);
			return limit(initial);
		}

		@Override
		protected MethodQuery createMethodQuery() {
			MethodQuery initial = MethodQuery.of(targetClass);
			return limit(initial);
		}

		private static <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limit(Q query) {
			return query
					.annotatedWith(Inject.class)
					.excludingModifier(Modifier.STATIC)
					.excludingModifier(Modifier.FINAL);
		}
	}
	
	abstract static class FilteredInjectionQuery<T, I> extends InjectionQuery<T, I> {
		private final InjectionQuery<T, I> parent;
		
		FilteredInjectionQuery(InjectionQuery<T, I> parent) {
			this.parent = parent;
		}

		protected abstract FieldQuery limitFieldsConcrete(FieldQuery query);
		protected abstract MethodQuery limitMethodsConcrete(MethodQuery query);

		@Override
		protected FieldQuery createFieldQuery() {
			FieldQuery parentQuery = this.parent.createFieldQuery();
			return limitFieldsConcrete(parentQuery);
		}

		@Override
		protected MethodQuery createMethodQuery() {
			MethodQuery parentQuery = this.parent.createMethodQuery();
			return limitMethodsConcrete(parentQuery);
		}
	}
	
	static final class NamedInjectionQuery<T, I> extends FilteredInjectionQuery<T, I> {
		private final String injectionName;
		
		NamedInjectionQuery(InjectionQuery<T, I> parent, String injectionName) {
			super(parent);
			this.injectionName = checkNotNull(injectionName);
		}

		@Override
		protected FieldQuery limitFieldsConcrete(FieldQuery query) {
			return limitConcrete(query);
		}

		@Override
		protected MethodQuery limitMethodsConcrete(MethodQuery query) {
			return limitConcrete(query);
		}

		private <M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>> Q limitConcrete(Q query) {
			SingleAnnotationFilter<Named> filter = AnnotationFilter.of(Named.class)
					.andMatching(annotation -> this.injectionName.equals(annotation.value()));
			return query.annotatedWith(filter);
		}
	}
	
	static final class TypedInjectionQuery<T, I, J> extends InjectionQuery<T, J> {
		// this cannot inherit from FilteredInjectionQuery because of changed types
		private final InjectionQuery<T, I> parent;
		private final Class<J> injectionClass;
		
		TypedInjectionQuery(InjectionQuery<T, I> parent, Class<J> injectionClass) {
			this.parent = parent;
			this.injectionClass = injectionClass;
		}

		@Override
		protected FieldQuery createFieldQuery() {
			FieldQuery parentQuery = this.parent.createFieldQuery();
			return parentQuery
					.typed(injectionClass);
		}

		@Override
		protected MethodQuery createMethodQuery() {
			MethodQuery parentQuery = this.parent.createMethodQuery();
			return parentQuery
					.parameters(injectionClass)
					.returningVoid();
		}
		
	}
}
