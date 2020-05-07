package org.perfectable.introspection.query;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class Streams {
	static <E> Stream<E> generateSingle(E initial,
										Function<? super E, ? extends Stream<? extends E>> mutator) {
		return generate(Stream.of(initial), mutator, element -> true);
	}

	static <E> Stream<E> generateSingleConditional(E initial,
												   Function<? super E, ? extends Stream<? extends E>> mutator,
												   Predicate<? super E> condition) {
		return generate(Stream.of(initial), mutator, condition);
	}

	private static <E> Stream<E> generate(Stream<E> initial,
										  Function<? super E, ? extends Stream<? extends E>> mutator,
										  Predicate<? super E> condition) {
		Spliterator<E> wrappedSpliterator =
			GeneratorSpliterator.wrap(initial.spliterator(), mutator, condition);
		return StreamSupport.stream(wrappedSpliterator, /* parallel= */false);
	}

	public static <E> Stream<E> from(Enumeration<E> enumeration) {
		Spliterator<E> spliterator = EnumerationSpliterator.create(enumeration);
		return StreamSupport.stream(spliterator, /* parallel= */false);
	}

	private static final class GeneratorSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
		private static final int ADDITIONAL_CHARACTERISTICS = 0;

		private final Spliterator<? extends T> wrapped;
		private final Function<? super @Nullable T, ? extends Stream<? extends T>> mutator;
		private final Predicate<? super T> condition;
		private final Deque<@Nullable T> buffer = new ArrayDeque<>();

		static <T> GeneratorSpliterator<T> wrap(Spliterator<? extends T> wrapped,
												Function<? super T, ? extends Stream<? extends T>> mutator,
												Predicate<? super T> condition) {
			return new GeneratorSpliterator<>(wrapped, mutator, condition);
		}

		private GeneratorSpliterator(Spliterator<? extends T> wrapped,
									 Function<? super @Nullable T, ? extends Stream<? extends T>> mutator,
									 Predicate<? super T> condition) {
			super(Long.MAX_VALUE, ADDITIONAL_CHARACTERISTICS);
			this.wrapped = wrapped;
			this.mutator = mutator;
			this.condition = condition;
		}

		@Override
		public boolean tryAdvance(Consumer<? super @Nullable T> consumer) {
			Consumer<? super @Nullable T> wrappedAction = (@Nullable T element) -> {
				consumer.accept(element);
				mutator.apply(element)
					.filter(condition)
					.forEach(buffer::add);
			};
			if (buffer.isEmpty()) {
				return wrapped.tryAdvance(wrappedAction);
			}
			else {
				@Nullable T generated = buffer.pop();
				wrappedAction.accept(generated);
				return true;
			}
		}
	}

	private static final class EnumerationSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
		private static final int ADDITIONAL_CHARACTERISTICS = 0;

		private final Enumeration<E> enumeration;

		private EnumerationSpliterator(Enumeration<E> enumeration) {
			super(Long.MAX_VALUE, ADDITIONAL_CHARACTERISTICS);
			this.enumeration = enumeration;
		}

		public static <X> Spliterator<X> create(Enumeration<X> enumeration) {
			return new EnumerationSpliterator<X>(enumeration);
		}

		@Override
		public boolean tryAdvance(Consumer<? super @Nullable E> consumer) {
			if (!enumeration.hasMoreElements()) {
				return false;
			}
			@SuppressWarnings("cast.unsafe")
			E nextElement = (@NonNull E) enumeration.nextElement();
			consumer.accept(nextElement);
			return true;
		}
	}

	private Streams() {
		// utility
	}
}
