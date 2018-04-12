package org.perfectable.introspection; // SUPPRESS LENGTH

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.perfectable.introspection.SimpleReflections.getConstructor;
import static org.perfectable.introspection.SimpleReflections.getMethod;

// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings("ClassCanBeStatic")
class FunctionalReferenceTest {
	private static final String NON_CONSTRUCTOR_REFERENCE_MESSAGE =
		"Interface implementation is not a constructor reference";
	private static final String NON_METHOD_REFERENCE_MESSAGE =
		"Interface implementation is not a method reference";

	@Nested
	class StaticMethodReference {
		private final TestParameterless marker = System::currentTimeMillis;
		private final Method markerMethod = getMethod(System.class, "currentTimeMillis");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(StaticMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
		}

		@Test
		void referencedMethodName() {
			assertThat(marker.introspect())
				.returns(markerMethod.getName(),
					FunctionalReference.Introspection::referencedMethodName);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitStatic() {
					return null;
				}
			});
		}
	}

	@Nested
	class InstanceMethodReference {
		private final TestSingleParameter marker = Object::hashCode;
		private final Method markerMethod = getMethod(Object.class, "hashCode");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(InstanceMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
		}

		@Test
		void referencedMethodName() {
			assertThat(marker.introspect())
				.returns(markerMethod.getName(),
					FunctionalReference.Introspection::referencedMethodName);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitInstance() {
					// pass
					return null;
				}
			});
		}
	}

	@Nested
	class BoundInstanceMethodReference {
		private final Object instance = new Object();
		private final TestParameterless marker = instance::hashCode;
		private final Method markerMethod = getMethod(Object.class, "hashCode");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(BoundInstanceMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
		}

		@Test
		void referencedMethodName() {
			assertThat(marker.introspect())
				.returns(markerMethod.getName(),
					FunctionalReference.Introspection::referencedMethodName);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitBound(Object boundInstance) {
					assertThat(boundInstance)
						.isSameAs(instance);
					return null;
				}
			});
		}
	}

	@Nested
	class ConstructorReference {
		private final TestWithResult marker = Object::new;
		private final Constructor<?> markerConstructor = getConstructor(Object.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(ConstructorReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedMethodName() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethodName())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedConstructor() {
			assertThat(marker.introspect())
				.returns(markerConstructor,
					FunctionalReference.Introspection::referencedConstructor);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitConstructor() {
					// pass
					return null;
				}
			});
		}
	}

	@Nested
	class AnnotationMethodReference {
		private final TestRetention marker = Retention::value;
		private final Method markerMethod = getMethod(Retention.class, "value");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(AnnotationMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
		}

		@Test
		void referencedMethodName() {
			assertThat(marker.introspect())
				.returns(markerMethod.getName(),
					FunctionalReference.Introspection::referencedMethodName);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitInstance() {
					// pass
					return null;
				}
			});
		}
	}

	@Nested
	class ParameterlessLambda {
		private final Object instance = new Object();
		@SuppressWarnings({"Convert2MethodRef", "ConstructorInvokesOverridable"})
		private final TestParameterless marker = () -> instance.hashCode();

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(ParameterlessLambda.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedMethodName() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethodName())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitLambda(Object... captures) {
					assertThat(captures)
						.hasSize(1)
						.hasOnlyElementsOfType(ParameterlessLambda.class);
					return null;
				}
			});
		}
	}

	@Nested
	class SingleParameterLambda {
		private final Object instance = new Object();
		@SuppressWarnings({"Convert2MethodRef", "ConstructorInvokesOverridable"})
		private final TestSingleParameter marker = parameter1 -> instance.hashCode();

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(SingleParameterLambda.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedMethodName() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethodName())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_METHOD_REFERENCE_MESSAGE);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitLambda(Object... captures) {
					assertThat(captures)
						.hasSize(1)
						.hasOnlyElementsOfType(SingleParameterLambda.class);
					return null;
				}
			});
		}
	}

	@Nested
	class OverloadedMethodReference {
		private final TestParameterless marker = System::getenv;
		private final Method method = getMethod(System.class, "getenv");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(OverloadedMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(method,
					FunctionalReference.Introspection::referencedMethod);
		}

		@Test
		void referencedMethodName() {
			assertThat(marker.introspect())
				.returns(method.getName(),
					FunctionalReference.Introspection::referencedMethodName);
		}

		@Test
		void referencedConstructor() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedConstructor())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage(NON_CONSTRUCTOR_REFERENCE_MESSAGE);
		}

		@Test
		void visit() {
			FunctionalReference.Introspection introspect = marker.introspect();
			introspect.visit(new TestVisitor() {
				@Override
				public Void visitStatic() {
					// pass
					return null;
				}
			});
		}
	}

	private static class TestVisitor implements FunctionalReference.Visitor<Void> {
		static final String INVALID_VISIT_MESSAGE = "Invalid visit";

		@Override
		public Void visitStatic() {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}

		@Override
		public Void visitInstance() {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}

		@Override
		public Void visitBound(Object boundInstance) {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}

		@Override
		public Void visitConstructor() {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}

		@Override
		public Void visitLambda(Object... captures) {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}
	}

	private interface TestParameterless extends FunctionalReference {
		void target();
	}

	private interface TestSingleParameter extends FunctionalReference {
		void target(Object parameter);
	}

	private interface TestWithResult extends FunctionalReference {
		Object target();
	}

	private interface TestRetention extends FunctionalReference {
		RetentionPolicy target(Retention retention);
	}
}
