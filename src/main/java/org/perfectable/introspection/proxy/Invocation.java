package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

public interface Invocation {

	@Nullable
	Object invoke() throws Throwable; // SUPPRESS IllegalThrows

	static Invocation returning(Object value) {
		return new Returning(value);
	}

	static Invocation throwing(Throwable thrown) {
		return new Throwing(thrown);
	}

	class Returning implements Invocation {
		@Nullable
		private final Object value;

		Returning(@Nullable Object value) {
			this.value = value;
		}

		@Nullable
		@Override
		public Object invoke() {
			return value;
		}
	}

	class Throwing implements Invocation {
		private final Throwable thrown;

		Throwing(Throwable thrown) {
			this.thrown = thrown;
		}

		@Nullable
		@Override
		public Object invoke() throws Throwable {
			throw thrown;
		}
	}

}
