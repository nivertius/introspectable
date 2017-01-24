package org.perfectable.introspection.query;

import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class InheritanceQuery<X> extends AbstractQuery<Class<? super X>, InheritanceQuery<X>> {
	public static <X> InheritanceQuery<X> of(Class<X> type) {
		return new Complete<>(type);
	}

	@Override
	public InheritanceQuery<X> filter(Predicate<? super Class<? super X>> filter) {
		return new Predicated<>(this, filter);
	}

	public InheritanceQuery<X> upToExcluding(Class<? super X> supertype) {
		return new BoundingExcluded<>(this, supertype);
	}

	public InheritanceQuery<X> upToIncluding(Class<? super X> supertype) {
		return new BoundingIncluded<>(this, supertype);
	}

	public InheritanceQuery<X> onlyInterfaces() {
		return new InterfacesOnly<>(this);
	}

	public InheritanceQuery<X> onlyClasses() {
		return new ClassesOnly<>(this);
	}

	InheritanceQuery() {
		// package extension only
	}

	private static class Complete<X> extends InheritanceQuery<X> {

		private final Class<X> initial;

		Complete(Class<X> initial) {
			this.initial = initial;
		}

		@Override
		public Stream<Class<? super X>> stream() {
			return Streams.generate(Stream.of(initial), InheritanceQuery::safeGetSupertypes);
		}
	}

	private abstract static class Filtered<X> extends InheritanceQuery<X> {

		private final InheritanceQuery<X> parent;

		Filtered(InheritanceQuery<X> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Class<? super X> candidate);

		@Override
		public Stream<Class<? super X>> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}

	private static final class Predicated<X> extends Filtered<X> {

		private final Predicate<? super Class<? super X>> filter;

		Predicated(InheritanceQuery<X> parent, Predicate<? super Class<? super X>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return filter.test(candidate);
		}
	}

	private static final class BoundingExcluded<X> extends Filtered<X> {

		private final Class<? super X> supertype;

		BoundingExcluded(InheritanceQuery<X> parent, Class<? super X> supertype) {
			super(parent);
			this.supertype = supertype;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return supertype.isAssignableFrom(candidate) && !supertype.equals(candidate);
		}
	}

	private static final class BoundingIncluded<X> extends Filtered<X> {

		private final Class<? super X> supertype;

		BoundingIncluded(InheritanceQuery<X> parent, Class<? super X> supertype) {
			super(parent);
			this.supertype = supertype;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return supertype.isAssignableFrom(candidate);
		}
	}

	private static final class InterfacesOnly<X> extends Filtered<X> {

		InterfacesOnly(InheritanceQuery<X> parent) {
			super(parent);
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return candidate.isInterface();
		}
	}

	private static final class ClassesOnly<X> extends Filtered<X> {

		ClassesOnly(InheritanceQuery<X> parent) {
			super(parent);
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return !candidate.isInterface();
		}
	}

	private static <X> Stream<Class<? super X>> safeGetSupertypes(Class<? super X> type) {
		Stream.Builder<Class<? super X>> builder = Stream.builder();
		@SuppressWarnings("unchecked")
		Class<? super X>[] interfaceArray = (Class<? super X>[]) type.getInterfaces();
		Stream.of(interfaceArray).forEach(builder);
		Class<? super X> superclass = type.getSuperclass();
		if (superclass != null) {
			builder.accept(superclass);
		}
		return builder.build();
	}
}
