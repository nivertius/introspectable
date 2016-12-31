package org.perfectable.introspection.query;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class Streams {
	static <E> Stream<E> generate(Stream<E> initial,
			Function<? super E, ? extends Stream<? extends E>> mutator) {
		return generate(initial, mutator, element -> true);
	}

	static <E> Stream<E> generate(Stream<E> initial,
								  Function<? super E, ? extends Stream<? extends E>> mutator,
								  Predicate<? super E> condition) {
		Spliterator<E> wrappedSpliterator =
				GeneratorSpliterator.wrap(initial.spliterator(), mutator, condition);
		return StreamSupport.stream(wrappedSpliterator, false);
	}

	private static final class GeneratorSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

		private static final int ADDITIONAL_CHARACTERISTICS = 0;

		private final Spliterator<? extends T> wrapped;
		private final Function<? super T, ? extends Stream<? extends T>> mutator;
		private final Predicate<? super T> condition;
		private final Deque<T> buffer = new LinkedList<>();

		static <T> GeneratorSpliterator<T> wrap(Spliterator<? extends T> wrapped,
												Function<? super T, ? extends Stream<? extends T>> mutator,
												Predicate<? super T> condition) {
			return new GeneratorSpliterator<>(wrapped, mutator, condition);
		}

		private GeneratorSpliterator(Spliterator<? extends T> wrapped,
									 Function<? super T, ? extends Stream<? extends T>> mutator,
									 Predicate<? super T> condition) {
			super(Long.MAX_VALUE, ADDITIONAL_CHARACTERISTICS);
			this.wrapped = wrapped;
			this.mutator = mutator;
			this.condition = condition;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			Consumer<? super T> wrappedAction = element -> {
				action.accept(element);
				mutator.apply(element).filter(condition).forEach(buffer::add);
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
