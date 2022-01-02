package org.perfectable.introspection;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MethodSignatureTest {
	private final ClassLoaderIntrospection loader = ClassLoaderIntrospection.SYSTEM;

	@Nested
	class Base {
		private static final String SIGNATURE_STRING = "(ILjava/lang/String;[I)J";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(int.class, String.class, int[].class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(long.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}
	@Nested
	class Named {
		private static final String SIGNATURE_STRING =
			"<F:Ljava/lang/Number;>testMethod(Ljava/lang/Class<TF;>;Ljava/lang/String;)TF;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void name() {
			assertThat(signature.name())
				.isEqualTo("testMethod");
		}

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Class.class, String.class);
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Number.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class Named {
		private static final String SIGNATURE_STRING =
			"<F:Ljava/lang/Number;>testMethod(Ljava/lang/Class<TF;>;Ljava/lang/String;)TF;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void name() {
			assertThat(signature.name())
				.isEqualTo("testMethod");
		}

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Class.class, String.class);
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Number.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class NumberBound {
		private static final String SIGNATURE_STRING =
			"<F:Ljava/lang/Number;>(Ljava/lang/Class<TF;>;Ljava/lang/String;)TF;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Class.class, String.class);
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Number.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class LowerBound {
		private static final String SIGNATURE_STRING =
			"<F:Ljava/lang/Object;>(TF;)Ljava/util/function/Function<-TF;TF;>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Object.class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Function.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class ExternalVariable {
		private static final String SIGNATURE_STRING =
			"(TS;)Ljava/util/function/Function<-TS;TS;>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThatThrownBy(() -> signature.runtimeParameterTypes(loader))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Cannot resolve parameter S");
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Function.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class Wildcard {
		private static final String SIGNATURE_STRING =
			"<F:Ljava/lang/Object;>(TF;)Ljava/util/function/Function<+TF;*>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Object.class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Function.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class DoubleBound {
		private static final String SIGNATURE_STRING =
			"<X:Ljava/lang/Long;F:TX;>(TF;)Ljava/util/function/Function<+TF;*>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Long.class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(Function.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class Throwing {
		private static final String SIGNATURE_STRING =
			"(Ljava/lang/Long;)V^Ljava/lang/Exception;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Long.class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(void.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.containsExactly(Exception.class);
		}
	}

	@Nested
	class ParametrizedResult {
		private static final String SIGNATURE_STRING =
			"(Ljava/lang/Long;)Ljava/util/List<Ljava/lang/Class;>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(Long.class);
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(List.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}

	@Nested
	class Complex {
		private static final String SIGNATURE_STRING =
			"<S:Ljava/lang/Object;T:Ljava/lang/Object;F:Ljava/lang/Object;"
			+ "R::Ljava/util/function/BiFunction<TS;TT;+TR;>;>"
			+ "(TR;Ljava/util/function/Function<TS;TF;>;Ljava/lang/Class<TT;>;)"
			+ "Ljava/util/function/BiFunction<TS;TT;TF;>;";
		private final MethodSignature signature = MethodSignature.read(SIGNATURE_STRING);

		@Test
		void runtimeParameterTypes() {
			assertThat(signature.runtimeParameterTypes(loader))
				.containsExactly(BiFunction.class, Function.class, Class.class);
		}
		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void name() {
			assertThat(signature.name())
				.isEmpty();
		}

		@Test
		void runtimeResultType() {
			assertThat(signature.runtimeResultType(loader))
				.isEqualTo(BiFunction.class);
		}

		@Test
		void runtimeDeclaredExceptionTypes() {
			assertThat(signature.runtimeDeclaredExceptionTypes(loader))
				.isEmpty();
		}
	}
}
