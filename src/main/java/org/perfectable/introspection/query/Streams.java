package org.perfectable.introspection.query;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class Streams {
	static <E> Stream<E> generate(Stream<E> initial,
			Function<? super E, ? extends Stream<? extends E>> mutator) {
		Spliterator<E> wrappedSpliterator = GeneratorSpliterator.wrap(initial.spliterator(), mutator);
		return StreamSupport.stream(wrappedSpliterator, false);
	}

	private static final class GeneratorSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

		private static final int ADDITIONAL_CHARACTERISTICS = 0;

		private final Spliterator<? extends T> wrapped;
		private final Function<? super T, ? extends Stream<? extends T>> mutator;
		private final Deque<T> buffer = new LinkedList<>();

		public static <T> GeneratorSpliterator<T> wrap(Spliterator<? extends T> wrapped,
													   Function<? super T, ? extends Stream<? extends T>> mutator) {
			return new GeneratorSpliterator<>(wrapped, mutator);
		}

		private GeneratorSpliterator(Spliterator<? extends T> wrapped,
									   Function<? super T, ? extends Stream<? extends T>> mutator) {
			super(Long.MAX_VALUE, ADDITIONAL_CHARACTERISTICS);
			this.wrapped = wrapped;
			this.mutator = mutator;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			Consumer<? super T> wrappedAction = element -> {
				action.accept(element);
				mutator.apply(element).forEach(buffer::add);
			};
			if (buffer.isEmpty()) {
				return wrapped.tryAdvance(wrappedAction);
			}
			else {
				T generated = buffer.pop();
				wrappedAction.accept(generated);
				return true;
			}
		}
	}

	private Streams() {
		// utility
	}
}
