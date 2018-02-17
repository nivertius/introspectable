package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

final class ReplacedReceiverInvocation<T, X extends T> implements Invocation<X> {
	private final Invocation<T> original;
	private final X replacement;

	public static <T, X extends T> Invocation<X> of(Invocation<T> original, X replacement) {
		return new ReplacedReceiverInvocation<>(original, replacement);
	}

	private ReplacedReceiverInvocation(Invocation<T> original, X replacement) {
		this.original = original;
		this.replacement = replacement;
	}

	@Nullable
	@Override
	public Object invoke() throws Throwable {
		return decompose(MethodInvocation::of).invoke();
	}

	@Override
	public <R> R decompose(Decomposer<? super X, R> decomposer) {
		Decomposer<T, R> replacementDecomposer =
			(method, receiver, arguments) -> decomposer.decompose(method, replacement, arguments);
		return original.decompose(replacementDecomposer);
	}

	@Override
	public <N extends X> Invocation<N> withReceiver(N newReceiver) {
		return new ReplacedReceiverInvocation<>(original, newReceiver);
	}
}
