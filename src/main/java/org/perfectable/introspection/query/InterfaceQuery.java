package org.perfectable.introspection.query;

import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class InterfaceQuery<X> extends AbstractQuery<Class<? super X>, InterfaceQuery<X>> {
	public static <X> InterfaceQuery<X> of(Class<X> type) {
		if (type.isInterface()) {
			return new OfInterface<>(type);
		}
		else {
			return new OfClass<>(type);
		}
	}

	@Override
	public InterfaceQuery<X> filter(Predicate<? super Class<? super X>> filter) {
		return new Predicated<>(this, filter);
	}

	private abstract static class Filtered<X> extends InterfaceQuery<X> {

		private final InterfaceQuery<X> parent;

		Filtered(InterfaceQuery<X> parent) {
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

		Predicated(InterfaceQuery<X> parent, Predicate<? super Class<? super X>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return filter.test(candidate);
		}
	}

	private static class OfInterface<X> extends InterfaceQuery<X> {

		private final Class<X> initialInterface;

		OfInterface(Class<X> initialInterface) {
			this.initialInterface = initialInterface;
		}

		@Override
		public Stream<Class<? super X>> stream() {
			return Stream.<Class<? super X>>of(initialInterface)
					.flatMap(element -> Stream.concat(Stream.of(element), InterfaceQuery.safeGetInterfaces(element)));
		}
	}

	private static class OfClass<X> extends InterfaceQuery<X> {

		private final Class<X> initialClass;

		OfClass(Class<X> initialClass) {
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
