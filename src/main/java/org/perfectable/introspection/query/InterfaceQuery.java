package org.perfectable.introspection.query;

import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class InterfaceQuery<X> extends AbstractQuery<Class<? super X>, InterfaceQuery<X>> {
	public static <X> InterfaceQuery<X> of(Class<X> type) {
		if (type.isInterface()) {
			return new InterfaceInterfaceQuery<>(type);
		}
		else {
			return new ClassInterfaceQuery<>(type);
		}
	}

	@Override
	public InterfaceQuery<X> filter(Predicate<? super Class<? super X>> filter) {
		return new PredicatedInterfaceQuery<>(this, filter);
	}

	private abstract static class FilteredInterfaceQuery<X> extends InterfaceQuery<X> {

		private final InterfaceQuery<X> parent;

		FilteredInterfaceQuery(InterfaceQuery<X> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Class<? super X> candidate);

		@Override
		public Stream<Class<? super X>> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}

	}

	private static final class PredicatedInterfaceQuery<X> extends FilteredInterfaceQuery<X> {

		private final Predicate<? super Class<? super X>> filter;

		PredicatedInterfaceQuery(InterfaceQuery<X> parent, Predicate<? super Class<? super X>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return filter.test(candidate);
		}
	}

	private static class InterfaceInterfaceQuery<X> extends InterfaceQuery<X> {

		private final Class<X> initialInterface;

		InterfaceInterfaceQuery(Class<X> initialInterface) {
			this.initialInterface = initialInterface;
		}

		@Override
		public Stream<Class<? super X>> stream() {
			return Stream.<Class<? super X>>of(initialInterface)
					.flatMap(element -> Stream.concat(Stream.of(element), InterfaceQuery.safeGetInterfaces(element)));
		}
	}

	private static class ClassInterfaceQuery<X> extends InterfaceQuery<X> {

		private final Class<X> initialClass;

		ClassInterfaceQuery(Class<X> initialClass) {
			this.initialClass = initialClass;
		}

		@Override
		public Stream<Class<? super X>> stream() {
			return InterfaceQuery.safeGetInterfaces(initialClass)
					.flatMap(element -> Stream.concat(Stream.of(element), InterfaceQuery.safeGetInterfaces(element)));
		}
	}

	private static <X> Stream<Class<? super X>> safeGetInterfaces(Class<? super X> type) {
		@SuppressWarnings("unchecked")
		Class<? super X>[] interfaceArray = (Class<? super X>[]) type.getInterfaces();
		return Stream.of(interfaceArray);
	}
}
