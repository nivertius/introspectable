package org.perfectable.introspection;

import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

@Immutable
public final class AnnotationBuilder<A extends Annotation> {

	public static <A extends Annotation> A marker(Class<A> annotationType) {
		checkAnnotationInterface(annotationType);
		checkArgument(annotationType.getDeclaredMethods().length == 0, "Annotation interface is not a marker");
		AnnotationInvocationHandler<A> invocationHandler =
			new AnnotationInvocationHandler<>(annotationType, ImmutableMap.of());
		return ProxyBuilder.forInterface(annotationType)
			.instantiate(invocationHandler);
	}

	public static <A extends Annotation> AnnotationBuilder<A> of(Class<A> annotationType) {
		checkAnnotationInterface(annotationType);
		return new AnnotationBuilder<>(annotationType, ImmutableMap.of());
	}

	@FunctionalInterface
	public interface MemberExtractor<A, X> extends FunctionalReference {
		@SuppressWarnings("unused")
		@CanIgnoreReturnValue
		X extract(A annotation);
	}

	public <X> AnnotationBuilder<A> with(MemberExtractor<A, X> member, X value) {
		requireNonNull(value);
		Method method = member.introspect().referencedMethod();
		Class<?> memberType = Primitives.wrap(method.getReturnType());
		checkArgument(memberType.isInstance(value),
			"Value %s cannot be provided for member %s of type %s", value, method.getName(), memberType);
		ImmutableMap<Method, Object> newValueMap = ImmutableMap.<Method, Object>builder()
			.putAll(valueMap).put(method, value).build();
		return new AnnotationBuilder<>(annotationType, newValueMap);
	}

	public A build() {
		validateMembers();
		AnnotationInvocationHandler<A> invocationHandler =
			new AnnotationInvocationHandler<>(annotationType, valueMap);
		return ProxyBuilder.forInterface(annotationType)
			.instantiate(invocationHandler);
	}

	private void validateMembers() throws IllegalStateException {
		for (Method method : annotationType.getDeclaredMethods()) {
			if (method.getDefaultValue() == null && !valueMap.containsKey(method)) {
				throw new IllegalStateException("No value set for member '" + method.getName() + "'");
			}
		}
	}

	private static void checkAnnotationInterface(Class<?> annotationType) {
		checkArgument(annotationType.isInterface()
				&& annotationType.getInterfaces().length == 1
				&& annotationType.getInterfaces()[0].equals(Annotation.class),
			"Provided class is not an annotation interface");
	}

	private final Class<A> annotationType;

	// values of this map are only immutable types that can be annotation type elements as declared by JLS 9.6.1
	@SuppressWarnings("Immutable")
	private final ImmutableMap<Method, Object> valueMap;

	private AnnotationBuilder(Class<A> annotationType, ImmutableMap<Method, Object> valueMap) {
		this.annotationType = annotationType;
		this.valueMap = valueMap;
	}

	private static final Method ANNOTATION_TYPE_METHOD = introspect(Annotation.class)
		.methods()
		.named("annotationType")
		.unique();

	@Immutable
	private static final class AnnotationInvocationHandler<A> implements InvocationHandler<MethodInvocation<A>> {
		private static final int UNCALCULATED_HASH_CODE = -1;
		private static final int MEMBER_NAME_HASH_MULTIPLIER = 127;

		private final Class<A> annotationType;

		// values of this map are only immutable types that can be annotation type elements as declared by JLS 9.6.1
		@SuppressWarnings("Immutable")
		private final ImmutableMap<Method, Object> valueMap;

		@SuppressWarnings("Immutable") // generated lazily once
		private volatile int cachedHashCode = UNCALCULATED_HASH_CODE;

		@Nullable
		@SuppressWarnings("Immutable") // generated lazily once
		private volatile String cachedRepresentation;

		AnnotationInvocationHandler(Class<A> annotationType, ImmutableMap<Method, Object> valueMap) {
			this.annotationType = annotationType;
			this.valueMap = valueMap;
		}

		@Nullable
		@Override
		public Object handle(MethodInvocation<A> invocation) {
			return invocation.decompose(this::calculateMethodResult);
		}

		Object calculateMethodResult(Method method, @SuppressWarnings("unused") A receiver, Object... arguments) {
			if (ObjectMethods.EQUALS.equals(method)) {
				return calculateEquals(arguments[0]);
			}
			if (ObjectMethods.HASH_CODE.equals(method)) {
				return calculateHash();
			}
			if (ObjectMethods.TO_STRING.equals(method)) {
				return calculateRepresentation();
			}
			if (ANNOTATION_TYPE_METHOD.equals(method)) {
				return annotationType;
			}
			return valueMap.getOrDefault(method, method.getDefaultValue());
		}

		boolean calculateEquals(Object other) {
			if (!(other instanceof Annotation)) {
				return false;
			}
			Annotation otherAnnotation = (Annotation) other;
			Class<? extends Annotation> otherAnnotationType = otherAnnotation.annotationType();
			if (!annotationType.equals(otherAnnotationType)) {
				return false;
			}
			for (Method method : annotationType.getDeclaredMethods()) {
				Object thisValue = valueMap.getOrDefault(method, method.getDefaultValue());
				Object otherValue = safeInvoke(method, other);
				if (!Objects.equals(thisValue, otherValue)) {
					return false;
				}
			}
			return true;
		}

		private int calculateHash() {
			if (cachedHashCode == UNCALCULATED_HASH_CODE) {
				synchronized (this) {
					int current = 0;
					for (Method method : annotationType.getDeclaredMethods()) {
						Object value = valueMap.getOrDefault(method, method.getDefaultValue());
						String name = method.getName();
						current += MEMBER_NAME_HASH_MULTIPLIER * name.hashCode() ^ Objects.hashCode(value);
					}
					cachedHashCode = current;
				}
			}
			return cachedHashCode;
		}

		@SuppressWarnings("ReturnMissingNullable")
		private String calculateRepresentation() {
			if (cachedRepresentation == null) {
				String elements = valueMap.entrySet().stream()
					.map(entry -> entry.getKey() + "=" + formatValue(entry.getValue()))
					.collect(Collectors.joining(", "));
				cachedRepresentation = "@" + annotationType.getName() + '(' + elements + ')';
			}
			return cachedRepresentation;
		}
	}

	private static String formatValue(Object value) {
		if (value instanceof String) {
			return "\"" + value + "\""; // SUPPRESS MultipleStringLiterals
		}
		return value.toString();
	}

	private static Object safeInvoke(Method method, Object target) {
		try {
			return method.invoke(target);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new AssertionError(e);
		}
	}
}
