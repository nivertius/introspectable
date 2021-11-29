package org.perfectable.introspection;

import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.concurrent.LazyInit;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.perfectable.introspection.Introspections.introspect;

/**
 * Builder pattern for creating annotation type instances.
 *
 * <p>Sometimes library methods require passing annotation instance, assuming that client will obtain it from
 * reflection on some member or type. This is often poor design, but sometimes some API actually is defined by
 * annotations rather than usual program structure.
 *
 * <p>In these cases, trying to call those methods, client has two straightforward options:
 * <ul>
 *     <li>Create token members or types with required annotation them and extract this annotation before call.</li>
 *     <li>Actually implement annotation interface, either by anonymous class place of call, or as a named somewhere
 *     near.</li>
 * </ul>
 *
 * <p>Both of those solutions are problematic. Beside introduction of unnecessary declaration, they also:
 * <ul>
 *     <li>If different annotation member values are needed, multiple synthetic annotation targets needs to be
 *     declared.</li>
 *     <li>Annotation interfaces as a rule should not be implemented, and static checkers will mark. Implemented
 *     interface will often have repeated constructs, and should have {@code toString}, {@code hashCode}
 *     and {@code equals} implemented, but often it doesn't.</li>
 * </ul>
 *
 * <p>This class helps create instances of annotation type that are indistinguishable from ones extracted by reflection.
 * These have same {@code toString} and {@code hashCode} and {@code equals} that works with native ones.
 *
 * <p>If annotation has no members (is effectively a <i>marker</i>), it can be completely constructed by {@link #marker}
 * call.
 *
 * <p>Otherwise annotation instance should be build starting with {@link #of}, with zero or more calls to {@link #with}
 * and then {@link #build}. Every member of the annotation (i.e. annotation interface method) that doesn't have default
 * value, needs to have value set, and this requirement is checked in the {@link #build} method.
 *
 * <p>Instances of this class are immutable, can be shared between threads and stored in global variables. Products of
 * this builder are also immutable, as are all annotation instances.
 *
 * <p>Example of marker annotation creation:
 * <pre>
 *     Immutable immutableInstance = AnnotationBuilder.marker(Immutable.class);
 * </pre>
 *
 * <p>Example of more complex annotation creation:
 * <pre>
 *     AnnotationBuilder&lt;Tag&gt; tagBuilder = AnnotationBuilder.of(Tag.class);
 *     Tag tag1 = tagBuilder.with(Tag::value, "one").build();
 *     Tag tag2 = tagBuilder.with(Tag::value, "two").build();
 *     Tags tags = AnnotationBuilder.of(Tags.class).with(Tags::value, new Tag[] { tag1, tag2 }).build();
 * </pre>
 *
 * @param <A> annotation type to build
 */
@Immutable
public final class AnnotationBuilder<A extends Annotation> {

	/**
	 * Creates instance of annotation with no members.
	 *
	 * <p>If annotation have members, but all of them are default, this method will still
	 *
	 * <p>This method is pure - with the same arguments will produce equivalent results.
	 *
	 * @param annotationType representation of type of annotation to build
	 * @param <A> type of annotation to build
	 * @return Only annotation instance that can be created for this annotation.
	 * @throws IllegalArgumentException when provided class does not represent an annotation interface
	 * @throws IllegalArgumentException when provided annotation type has any members
	 */
	public static <A extends Annotation> A marker(Class<A> annotationType) {
		checkAnnotationInterface(annotationType);
		checkArgument(annotationType.getDeclaredMethods().length == 0, "Annotation interface is not a marker");
		AnnotationInvocationHandler<A> invocationHandler =
			new AnnotationInvocationHandler<>(annotationType, ImmutableMap.of());
		return ProxyBuilder.forInterface(annotationType)
			.instantiate(invocationHandler);
	}

	/**
	 * Creates unconfigured builder for specified annotation type.
	 *
	 * @param annotationType representation of type of annotation to build
	 * @param <A> type of annotation to build
	 * @return Fresh, unconfigured builder
	 * @throws IllegalArgumentException when provided class does not represent an annotation interface
	 */
	public static <A extends Annotation> AnnotationBuilder<A> of(Class<A> annotationType) {
		checkAnnotationInterface(annotationType);
		return new AnnotationBuilder<>(annotationType, ImmutableMap.of());
	}

	/**
	 * Functional reference interface which allows type-safe and refactor-safe extraction of members.
	 *
	 * <p>This interface should only be implemented with method reference for a method that is a member of an
	 * annotation. The method is never actually called in this class.
	 *
	 * @param <A> annotation type to mark member on
	 * @param <X> annotation member type
	 */
	@FunctionalInterface
	public interface MemberExtractor<A extends Annotation, X> extends FunctionalReference {
		@SuppressWarnings({"unused", "javadoc"})
		@CanIgnoreReturnValue
		X extract(A annotation);
	}

	/**
	 * Configures member of created annotation instances to have specified value.
	 *
	 * <p>Member is selected by client by providing method reference.
	 *
	 * <p>Builder will reject setting value for a member that has already value configured.
	 *
	 * @param member reference to method which represents a configured member
	 * @param value value for specified member in annotation instance
	 * @param <X> type of member
	 * @return new builder, with additional member configured
	 * @throws IllegalArgumentException when member is not a method reference to member of this builders annotation type
	 *     or when value is not actually instance of member type
	 */
	public <X extends @NonNull Object> AnnotationBuilder<A> with(MemberExtractor<A, X> member, X value) {
		Method method;
		try {
			method = member.introspect().referencedMethod();
		}
		catch (IllegalStateException e) {
			throw new IllegalArgumentException(e);
		}
		checkArgument(method.getDeclaringClass().equals(annotationType),
			"Extractor should be a reference to method declared by annotation " + annotationType);
		Class<?> memberType = Primitives.wrap(method.getReturnType());
		checkArgument(memberType.isInstance(value),
			"Value %s cannot be provided for member %s of type %s", value, method.getName(), memberType);
		ImmutableMap<Method, Object> newValueMap = ImmutableMap.<Method, Object>builder()
			.putAll(valueMap).put(method, value).build();
		return new AnnotationBuilder<>(annotationType, newValueMap);
	}

	/**
	 * Creates annotation instance.
	 *
	 * <p>Performs member validation: each non-default member must have configured value.
	 *
	 * @return annotation instance with configured members
	 */
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
		private static final int MEMBER_NAME_HASH_MULTIPLIER = 127;

		private final Class<A> annotationType;

		// values of this map are only immutable types that can be annotation type elements as declared by JLS 9.6.1
		@SuppressWarnings("Immutable")
		private final ImmutableMap<Method, Object> valueMap;

		@LazyInit
		private volatile @MonotonicNonNull Integer cachedHashCode;
		@LazyInit
		private volatile @MonotonicNonNull String cachedRepresentation;

		AnnotationInvocationHandler(Class<A> annotationType, ImmutableMap<Method, Object> valueMap) {
			this.annotationType = annotationType;
			this.valueMap = valueMap;
		}

		@Override
		public @Nullable Object handle(MethodInvocation<A> invocation) {
			MethodInvocation.Decomposer<A, @Nullable Object> decomposer = this::calculateMethodResult;
			@Nullable Object result = invocation.decompose(decomposer);
			return result;
		}

		private @Nullable Object calculateMethodResult(Method method, @SuppressWarnings("unused") A receiver,
									 @Nullable Object... arguments) {
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

		boolean calculateEquals(@Nullable Object other) {
			if (!(other instanceof Annotation)) {
				return false;
			}
			Annotation otherAnnotation = (Annotation) other;
			Class<? extends Annotation> otherAnnotationType = otherAnnotation.annotationType();
			if (!annotationType.equals(otherAnnotationType)) {
				return false;
			}
			for (Method method : annotationType.getDeclaredMethods()) {
				@Nullable Object thisValue = valueMap.getOrDefault(method, method.getDefaultValue());
				@Nullable Object otherValue = safeInvoke(method, other);
				if (!Objects.equals(thisValue, otherValue)) {
					return false;
				}
			}
			return true;
		}

		@EnsuresNonNull("cachedHashCode")
		private int calculateHash() {
			if (cachedHashCode == null) {
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

		@EnsuresNonNull("cachedRepresentation")
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
			return String.format("\"%s\"", value);
		}
		return value.toString();
	}

	private static @Nullable Object safeInvoke(Method method, Object target) {
		try {
			return method.invoke(target);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new LinkageError(e.getMessage(), e);
		}
	}
}
