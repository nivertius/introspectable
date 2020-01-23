package org.perfectable.introspection; // SUPPRESS FILE FileLength

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.assertj.core.api.iterable.Extractor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.perfectable.introspection.SimpleReflections.getConstructor;
import static org.perfectable.introspection.SimpleReflections.getMethod;

// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings("ClassCanBeStatic")
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.annotatedParameter(0));
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(StringBuilder.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
				public Void visitInstance(Method method) {
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(StringBuilder.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
		}

		@Test
		void parametersOneType() {
			assertThat(marker.introspect())
				.returns(StringBuffer.class, introspection -> introspection.parameterType(1));
		}

		@Test
		void parametersOneGenericType() {
			assertThat(marker.introspect())
				.returns(StringBuffer.class, introspection -> introspection.genericParameterType(1));
		}

		@Test
		void parametersOneAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(1))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(StringBuffer.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(Retention.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			Extractor<Annotation, Class<?>> extractor =
				(Annotation annotation) -> (Class<?>) annotation.annotationType();
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> {
						assertThat(annotatedElement.getAnnotations())
							.extracting(extractor)
							.containsExactly(Documented.class, Retention.class, Target.class);
					}));
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
				public Void visitInstance(Method method) {
					assertThat(method)
						.isEqualTo(markerMethod);
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
		void parametersZeroGenericType() {
			assertThat(marker.introspect())
				.returns(Object.class, introspection -> introspection.genericParameterType(0));
		}

		@Test
		void parametersZeroAnnotated() {
			assertThat(marker.introspect())
				.satisfies(introspection -> assertThat(introspection.annotatedParameter(0))
					.satisfies(annotatedElement -> assertThat(annotatedElement.getAnnotations())
						.isEmpty()));
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
		private final Method markerMethod = getMethod(System.class, "getenv");

		@Test
		void capturingType() {
			assertThat(marker.introspect())
				.returns(OverloadedMethodReference.class,
					FunctionalReference.Introspection::capturingType);
		}

		@Test
		void resultType() {
			assertThat(marker.introspect())
				.returns(java.util.Map.class,
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
				public Void visitStatic(Method method) {
					assertThat(method)
						.isEqualTo(markerMethod);
					return null;
				}
			});
		}
	}

	private static class TestVisitor extends FunctionalReference.PartialVisitor<Void> {
		static final String INVALID_VISIT_MESSAGE = "Invalid visit";

		@Override
		protected Void fallback() {
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
}
