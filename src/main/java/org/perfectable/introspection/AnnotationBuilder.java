package org.perfectable.introspection;

import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static com.google.common.base.Preconditions.checkArgument;
import static org.perfectable.introspection.Introspections.introspect;

public final class AnnotationBuilder<A extends Annotation> {

	public static <A extends Annotation> A marker(Class<A> annotationType) {
		return of(annotationType).build();
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
		String name = member.introspect().referencedMethodName();
		ImmutableMap<String, Object> newValueMap = ImmutableMap.<String, Object>builder()
			.putAll(valueMap).put(name, value).build();
		return new AnnotationBuilder<>(annotationType, newValueMap);
	}

	public A build() {
		validateMembers();
		return ProxyBuilderFactory.any().ofInterfaces(annotationType)
			.instantiate(invocationHandler);
	}

	private void validateMembers() throws IllegalStateException {
		for (Method method : annotationType.getDeclaredMethods()) {
			String memberName = method.getName();
			if (method.getDefaultValue() == null && !valueMap.containsKey(memberName)) {
				throw new IllegalStateException("No value set for member '" + memberName + "'");
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
	private final ImmutableMap<String, Object> valueMap;

	private final AnnotationInvocationHandler invocationHandler = new AnnotationInvocationHandler();

	private AnnotationBuilder(Class<A> annotationType, ImmutableMap<String, Object> valueMap) {
		this.annotationType = annotationType;
		this.valueMap = valueMap;
	}

	private static final Joiner.MapJoiner MEMBER_JOINER = Joiner.on(", ").withKeyValueSeparator('=');

	private static final Method ANNOTATION_TYPE_METHOD = introspect(Annotation.class)
		.methods()
		.named("annotationType")
		.unique();

	private final class AnnotationInvocationHandler implements InvocationHandler<MethodInvocation<A>> {
		private static final int UNCALCULATED_HASH_CODE = -1;
		private static final int MEMBER_NAME_HASH_MULTIPLIER = 127;

		private volatile int cachedHashCode = UNCALCULATED_HASH_CODE;
		@Nullable
		private volatile String cachedRepresentation;

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
			return valueMap.getOrDefault(method.getName(), method.getDefaultValue());
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
				String name = method.getName();
				Object thisValue = valueMap.getOrDefault(name, method.getDefaultValue());
				Object otherValue = safeInvoke(otherAnnotationType, name, other);
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
						String name = method.getName();
						Object value = valueMap.getOrDefault(name, method.getDefaultValue());
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
				synchronized (this) {
					StringBuilder representationBuilder = new StringBuilder("@")
						.append(annotationType.getName()).append('(');
					MEMBER_JOINER.appendTo(representationBuilder, valueMap);
					representationBuilder.append(')');
					cachedRepresentation = representationBuilder.toString();
				}
			}
			return cachedRepresentation;
		}
	}

	private static Object safeInvoke(Class<?> declaringClass, String name, Object target) {
		try {
			Method method = declaringClass.getDeclaredMethod(name);
			return method.invoke(target);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}
}
