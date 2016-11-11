package org.perfectable.introspection.proxy;


final class ForwardingHandler<T> implements InvocationHandler<T> {

	private T target;

	static <T> ForwardingHandler<T> of(T target) {
		return new ForwardingHandler<>(target);
	}

	private ForwardingHandler(T target) {
		this.target = target;
	}

	public void swap(T newTarget) {
		this.target = newTarget;
	}

	@Override
	public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
		@SuppressWarnings("unchecked")
		MethodBoundInvocation<? extends T> methodInvocation = (MethodBoundInvocation<? extends T>) invocation;
		MethodBoundInvocationMappingDecomposer<T> decomposer =
				MethodBoundInvocationMappingDecomposer.<T>identity().withReceiverTransformer(receiver -> this.target);
		BoundInvocation<?> replaced = methodInvocation.decompose(decomposer);
		return replaced.invoke();
	}

}
