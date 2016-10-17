package org.perfectable.introspection.query;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.perfectable.introspection.InheritanceChain;

public abstract class FieldQuery extends MemberQuery<Field, FieldQuery> {
	
	public static <X> FieldQuery of(Class<X> type) {
		checkNotNull(type);
		return new CompleteFieldQuery<>(type);
	}
	
	@Override
	public FieldQuery named(String name) {
		checkNotNull(name);
		return new NamedFieldQuery(this, name);
	}
	
	@Override
	public FieldQuery matching(Predicate<? super Field> filter) {
		checkNotNull(filter);
		return new PredicatedFieldQuery(this, filter);
	}
	
	@Override
	public FieldQuery typed(Class<?> type) {
		checkNotNull(type);
		return new TypedFieldQuery(this, type);
	}
	
	@Override
	public FieldQuery annotatedWith(AnnotationFilter annotationFilter) {
		checkNotNull(annotationFilter);
		return new AnnotatedFieldQuery(this, annotationFilter);
	}
	
	@Override
	public FieldQuery excludingModifier(int excludedModifier) {
		return new ExcludedModifierFieldQuery(this, excludedModifier);
	}
	
	private static final class CompleteFieldQuery<X> extends FieldQuery {
		private final InheritanceChain<X> chain;
		
		public CompleteFieldQuery(Class<X> type) {
			this.chain = InheritanceChain.startingAt(type);
		}
		
		@Override
		public Stream<Field> stream() {
			return this.chain.stream()
					.flatMap(testedClass -> Stream.of(testedClass.getDeclaredFields()));
		}
	}
	
	private abstract static class FilteredFieldQuery extends FieldQuery {
		private final FieldQuery parent;
		
		public FilteredFieldQuery(FieldQuery parent) {
			this.parent = parent;
		}
		
		protected abstract boolean matches(Field candidate);
		
		@Override
		public Stream<Field> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}
	
	private static class NamedFieldQuery extends FilteredFieldQuery {
		private final String name;
		
		public NamedFieldQuery(FieldQuery parent, String name) {
			super(parent);
			this.name = name;
		}
		
		@Override
		protected boolean matches(Field candidate) {
			return this.name.equals(candidate.getName());
		}
	}
	
	private static class PredicatedFieldQuery extends FilteredFieldQuery {
		private final Predicate<? super Field> filter;
		
		public PredicatedFieldQuery(FieldQuery parent, Predicate<? super Field> filter) {
			super(parent);
			this.filter = filter;
		}
		
		@Override
		protected boolean matches(Field candidate) {
			return this.filter.test(candidate);
		}
	}
	
	private static class TypedFieldQuery extends FilteredFieldQuery {
		private final Class<?> type;
		
		public TypedFieldQuery(FieldQuery parent, Class<?> type) {
			super(parent);
			this.type = type;
		}
		
		@Override
		protected boolean matches(Field candidate) {
			return this.type.isAssignableFrom(candidate.getType());
		}
	}
	
	private class AnnotatedFieldQuery extends FilteredFieldQuery {
		private final AnnotationFilter annotationFilter;
		
		public AnnotatedFieldQuery(FieldQuery parent, AnnotationFilter annotationFilter) {
			super(parent);
			this.annotationFilter = annotationFilter;
		}
		
		@Override
		protected boolean matches(Field candidate) {
			return this.annotationFilter.appliesOn(candidate);
		}
	}
	
	private static class ExcludedModifierFieldQuery extends FilteredFieldQuery {
		private final int excludedModifier;
		
		public ExcludedModifierFieldQuery(FieldQuery parent, int excludedModifier) {
			super(parent);
			this.excludedModifier = excludedModifier;
		}
		
		@Override
		protected boolean matches(Field candidate) {
			return (candidate.getModifiers() & this.excludedModifier) == 0;
		}
	}
	
	protected FieldQuery() {
		// no default fields
	}
	
}
