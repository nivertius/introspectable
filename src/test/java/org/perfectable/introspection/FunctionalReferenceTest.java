package org.perfectable.introspection; // SUPPRESS FILE FileLength

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.perfectable.introspection.SimpleReflections.getConstructor;
import static org.perfectable.introspection.SimpleReflections.getMethod;

// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings({"ClassCanBeStatic", "Convert2MethodRef", "ResultOfMethodCallIgnored"})
class FunctionalReferenceTest { // SUPPRESS ExcessiveClassLength NcssCount
	private static final String NON_CONSTRUCTOR_REFERENCE_MESSAGE =
		"Interface implementation is not a constructor reference";
	private static final String NON_METHOD_REFERENCE_MESSAGE =
		"Interface implementation is not a method reference";

	@Nested
	class StaticMethodReferenceParameterless {
		private final TestParameterless marker = System::currentTimeMillis;
		private final Method markerMethod = getMethod(System.class, "currentTimeMillis");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(StaticMethodReferenceParameterless.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(long.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(0, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitStatic(Method method) {
					assertThat(method).isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}

	@Nested
	class StaticMethodReferenceGeneric {
		private final TestWithGenerics<Object, Integer> marker = System::identityHashCode;
		private final Method markerMethod = getMethod(System.class, "identityHashCode", Object.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(StaticMethodReferenceGeneric.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(int.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitStatic(Method method) {
					assertThat(method).isEqualTo(markerMethod);
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
		void resultType() {
			assertThat(marker.introspect())
				.returns(int.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitInstance(Method method) {
					assertThat(method).isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}

	@Nested
	class InstanceMethodReferenceWithGenerics {
		private final TestWithGenerics<StringBuilder, String> marker = StringBuilder::toString;
		private final Method markerMethod = getMethod(StringBuilder.class, "toString");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(InstanceMethodReferenceWithGenerics.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(String.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(StringBuilder.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public @Nullable Void visitInstance(Method method) {
					assertThat(method).isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}

	@Nested
	class InstanceMethodReferenceWithDoubleGenerics {
		private final TestDoubleWithGenerics<StringBuilder, StringBuffer, StringBuilder> marker = StringBuilder::append;
		private final Method markerMethod = getMethod(StringBuilder.class, "append", StringBuffer.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(InstanceMethodReferenceWithDoubleGenerics.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(StringBuilder.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(2, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(StringBuilder.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void parametersOneType() {
			assertThat(marker.introspect())
				.returns(StringBuffer.class, introspection -> introspection.parameterType(1));
		}

		@Test
		void parametersOneAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(1)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitInstance(Method method) {
					assertThat(method).isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}


	@Nested
	class BoundInstanceMethodReferenceParameterless {
		private final Object instance = new Object();
		private final TestParameterless marker = instance::hashCode;
		private final Method markerMethod = getMethod(Object.class, "hashCode");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(BoundInstanceMethodReferenceParameterless.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(int.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(0, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitBound(Method method, Object boundInstance) {
					assertThat(method).isEqualTo(markerMethod);
					assertThat(boundInstance)
						.isSameAs(instance);
					return null;
				}
			});
		}
	}

	@Nested
	class BoundInstanceMethodReferenceSingleParameter {
		private final Object instance = new Object();
		private final TestSingleParameter marker = instance::equals;
		private final Method markerMethod = getMethod(Object.class, "equals", Object.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(BoundInstanceMethodReferenceSingleParameter.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(boolean.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitBound(Method method, Object boundInstance) {
					assertThat(method).isEqualTo(markerMethod);
					assertThat(boundInstance)
						.isSameAs(instance);
					return null;
				}
			});
		}
	}

	@Nested
	class ConstructorReferenceObject {
		private final TestWithResult marker = Object::new;
		private final Constructor<?> markerConstructor = getConstructor(Object.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(ConstructorReferenceObject.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(Object.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(0, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
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
				public Void visitConstructor(Constructor<?> constructor) {
					assertThat(constructor).isEqualTo(markerConstructor);
					return null;
				}
			});
		}
	}


	@Nested
	class ConstructorReferenceString {
		private final TestWithGenerics<StringBuffer, String> marker = String::new;
		private final Constructor<?> markerConstructor = getConstructor(String.class, StringBuffer.class);

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(ConstructorReferenceString.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(String.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(StringBuffer.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
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
				public Void visitConstructor(Constructor<?> constructor) {
					assertThat(constructor).isEqualTo(markerConstructor);
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
		void resultType() {
			assertThat(marker.introspect())
				.returns(RetentionPolicy.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(Retention.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitInstance(Method method) {
					assertThat(method)
						.isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}


	@Nested
	class MethodParameterAnnotations {
		private final TestSingleParameter marker = Declarator::staticSingle;

		@SuppressWarnings("argument.type.incompatible")
		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0))
					.satisfies(annotations -> assertThat(annotations).extracting(Annotation::annotationType)
						.satisfies(annotationTypes ->
							Assertions.<Class<? extends Annotation>>assertThat(annotationTypes)
								.containsExactly(TestAnnotation.class))));
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
		void resultType() {
			assertThat(marker.introspect())
				.returns(void.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(0, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
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
				public Void visitLambda(Method method, List<Object> captures) {
					assertThat(method).isNotNull();
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
		void resultType() {
			assertThat(marker.introspect())
				.returns(void.class,
					FunctionalReference.Introspection::resultType);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(1, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void parametersZeroType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.parameterType(0));
		}

		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0)).isEmpty());
		}

		@Test
		void referencedMethod() {
			FunctionalReference.Introspection introspect = marker.introspect();
			assertThatThrownBy(() -> introspect.referencedMethod())
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
				public Void visitLambda(Method method, List<Object> captures) {
					assertThat(method).isNotNull();
					assertThat(captures)
						.hasSize(1)
						.hasOnlyElementsOfType(SingleParameterLambda.class);
					return null;
				}
			});
		}
	}


	@Nested
	class AnnotatedParameterLambda {
		private final TestSingleParameter marker = (@TestAnnotation Object parameter1) -> { /* empty */ };

		@SuppressWarnings("argument.type.incompatible")
		@Disabled("Bug in JDK results in non-retention of annotations")
		@Test
		void parametersZeroAnnotations() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.parameterAnnotations(0))
					.satisfies(annotations -> assertThat(annotations).extracting(Annotation::annotationType)
						.satisfies(annotationTypes ->
							Assertions.<Class<? extends Annotation>>assertThat(annotationTypes)
								.containsExactly(TestAnnotation.class))));
		}
	}

	@Nested
	class OverloadedMethodReference {
		private final TestParameterless marker = System::getenv;
		private final Method markerMethod = getMethod(System.class, "getenv");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(OverloadedMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			FunctionalReference.Introspection introspection = marker.introspect();
			Type resultType = introspection.resultType();
			assertThat(resultType).isInstanceOf(ParameterizedType.class);
			ParameterizedType parameterizedResultType = (ParameterizedType) resultType;
			assertThat(parameterizedResultType)
				.returns(java.util.Map.class, ParameterizedType::getRawType)
				.returns(new Class<?>[] { String.class, String.class }, ParameterizedType::getActualTypeArguments);
		}

		@Test
		void parametersCount() {
			assertThat(marker.introspect())
				.returns(0, FunctionalReference.Introspection::parametersCount);
		}

		@Test
		void referencedMethod() {
			assertThat(marker.introspect())
				.returns(markerMethod,
					FunctionalReference.Introspection::referencedMethod);
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
				public Void visitStatic(Method method) {
					assertThat(method)
						.isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}

	private static class TestVisitor extends FunctionalReference.PartialVisitor<@Nullable Void> {
		static final String INVALID_VISIT_MESSAGE = "Invalid visit";

		@Override
		protected @Nullable Void fallback() {
			fail(INVALID_VISIT_MESSAGE);
			return null;
		}
	}

	@SuppressWarnings("unused")
	private interface TestParameterless extends FunctionalReference {
		void target();
	}

	@SuppressWarnings("unused")
	private interface TestSingleParameter extends FunctionalReference {
		void target(Object parameter);
	}

	@SuppressWarnings("unused")
	private interface TestWithResult extends FunctionalReference {
		Object target();
	}

	@SuppressWarnings("unused")
	private interface TestWithGenerics<S, T> extends FunctionalReference {
		T target(S parameter);
	}

	@SuppressWarnings("unused")
	private interface TestDoubleWithGenerics<S, W, T> extends FunctionalReference {
		T target(S parameter1, W parameter2);
	}

	@SuppressWarnings("unused")
	private interface TestRetention extends FunctionalReference {
		RetentionPolicy target(Retention retention);
	}

	@SuppressWarnings("PMD.UseUtilityClass")
	private static class Declarator {
		@SuppressWarnings("Deprecated")
		static void staticSingle(@TestAnnotation Object parameter1) {
			// empty
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface TestAnnotation {

	}
}
