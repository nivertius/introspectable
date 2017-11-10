package org.perfectable.introspection.query;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class RelatedTypeQuery extends AbstractQuery<Class<?>, RelatedTypeQuery> {
	public static RelatedTypeQuery of(Class<?> initial) {
		return new Complete(initial);
	}

	@Override
	public RelatedTypeQuery filter(Predicate<? super Class<?>> filter) {
		requireNonNull(filter);
		return new Predicated(this, filter);
	}

	RelatedTypeQuery() {
		// package extension only
	}

	private static class Complete extends RelatedTypeQuery {
		private final Class<?> type;

		Complete(Class<?> type) {
			this.type = type;
		}

		@Override
		public Stream<Class<?>> stream() {
			HashSet<Class<?>> seen = new HashSet<>();
			seen.add(type);
			return Streams.generateSingleConditional(type, RelatedTypeQuery::extractRelated, seen::add);
		}
	}

	private abstract static class Filtered extends RelatedTypeQuery {

		private final RelatedTypeQuery parent;

		Filtered(RelatedTypeQuery parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Class<?> candidate);

		@Override
		public Stream<Class<?>> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Class<?>)) {
				return false;
			}
			Class<?> candidateClass = (Class<?>) candidate;
			return matches(candidateClass) && parent.contains(candidate);
		}
	}

	private static final class Predicated extends Filtered {

		private final Predicate<? super Class<?>> filter;

		Predicated(RelatedTypeQuery parent, Predicate<? super Class<?>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<?> candidate) {
			return filter.test(candidate);
		}
	}

	private static Stream<Class<?>> extractRelated(Class<?> current) {
		Stream.Builder<Class<?>> resultBuilder = Stream.builder();
		extractEnclosingClasses(current).forEach(resultBuilder::add);
		extractNestedClasses(current).forEach(resultBuilder::add);
		extractFieldClasses(current).forEach(resultBuilder::add);
		extractMethodClasses(current).forEach(resultBuilder::add);
		extractParameterClasses(current).forEach(resultBuilder::add);
		extractSuperClasses(current).forEach(resultBuilder::add);
		return resultBuilder.build();
	}

	private static Stream<Class<?>> extractSuperClasses(Class<?> current) {
		// TODO this should work on generic superclass
		Stream.Builder<Class<?>> resultBuilder = Stream.builder();
		if (current.getSuperclass() != null) {
			resultBuilder.add(current.getSuperclass());
		}
		Stream.of(current.getInterfaces()).forEach(resultBuilder::add);
		return resultBuilder.build();
	}

	private static Stream<Class<?>> extractEnclosingClasses(Class<?> current) {
		Class<?> enclosingClass = current.getEnclosingClass();
		if (enclosingClass == null) {
			return Stream.empty();
		}
		return Stream.of(enclosingClass);
	}

	private static Stream<Class<?>> extractNestedClasses(Class<?> current) {
		return Stream.of(current.getClasses());
	}

	private static Stream<Class<?>> extractFieldClasses(Class<?> current) {
		return Stream.of(current.getDeclaredFields())
				.map(Field::getType);
	}

	private static Stream<Class<?>> extractMethodClasses(Class<?> current) {
		// TODO this should work on generic parameter and return types
		return Stream.of(current.getDeclaredMethods())
				.flatMap(method -> Stream.concat(
						Stream.concat(
								Stream.of(method.getReturnType()),
								Stream.of(method.getParameterTypes())),
						Stream.of(method.getExceptionTypes())
						)
				);
	}

	private static Stream<Class<?>> extractParameterClasses(Class<?> current) {
		return Stream.of(current.getTypeParameters())
				.flatMap(variable -> Stream.of(variable.getBounds()))
				.filter(type -> type instanceof Class)
				.map(type -> (Class<?>) type);
	}

}
