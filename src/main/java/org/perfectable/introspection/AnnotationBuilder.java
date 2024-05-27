package org.perfectable.introspection;

import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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
		checkArgument(value != null,
			"Null cannot be provided for member %s of type %s", method.getName(), memberType);
		checkArgument(memberType.isInstance(value),
			"Value %s (%s) cannot be provided for member %s of type %s", value, value.getClass(),
			method.getName(), memberType);
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
	private static final class AnnotationInvocationHandler<A>
			implements InvocationHandler<@Nullable Object, RuntimeException, MethodInvocation<A>> {
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
			return invocation.decompose(
				(MethodInvocation.Decomposer<A, @Nullable Object>) this::calculateMethodResult);
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
			return getMember(method);
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
				Object thisValue = getMember(method);
				@Nullable Object otherValue = safeInvoke(method, other);
				if (otherValue == null) {
					return false;
				}
				if (!memberEquals(thisValue, otherValue)) {
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
						String name = method.getName();
						Object value = getMember(method);
						current += MEMBER_NAME_HASH_MULTIPLIER * name.hashCode() ^ memberHashCode(value);
					}
					cachedHashCode = current;
				}
			}
			return cachedHashCode;
		}

		@SuppressWarnings("MultipleStringLiterals")
		@EnsuresNonNull("cachedRepresentation")
		private String calculateRepresentation() {
			if (cachedRepresentation == null) {
				synchronized (this) {
					StringBuilder builder = new StringBuilder("@").append(annotationType.getName()).append("(");
					boolean written = false;
					for (Method method : annotationType.getDeclaredMethods()) {
						builder.append(written ? ", " : "");
						written = true;
						Object value = getMember(method);
						builder.append(method.getName()).append("=").append(formatValue(value));
					}
					builder.append(")");
					cachedRepresentation = builder.toString();
				}
			}
			return cachedRepresentation;
		}

		private Object getMember(Method method) {
			@Nullable Object explicitValue = valueMap.get(method);
			if (explicitValue != null) {
				return explicitValue;
			}
			@Nullable Object defaultValue = method.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}
			throw new IllegalStateException("No default value for " + method);
		}
	}


	@SuppressWarnings({"OverlyComplexMethod", "NeedBraces", "NPathComplexity", "CyclomaticComplexity"})
	private static int memberHashCode(Object value) {
		Class<? extends @NonNull Object> valueClass = value.getClass();
		if (!valueClass.isArray()) return value.hashCode();
		Class<? extends @Nullable Object> componentType = valueClass.getComponentType();
		if (componentType.equals(byte.class)) return Arrays.hashCode((byte[]) value);
		if (componentType.equals(char.class)) return Arrays.hashCode((char[]) value);
		if (componentType.equals(double.class)) return Arrays.hashCode((double[]) value);
		if (componentType.equals(float.class)) return Arrays.hashCode((float[]) value);
		if (componentType.equals(int.class)) return Arrays.hashCode((int[]) value);
		if (componentType.equals(long.class)) return Arrays.hashCode((long[]) value);
		if (componentType.equals(short.class)) return Arrays.hashCode((short[]) value);
		if (componentType.equals(boolean.class)) return Arrays.hashCode((boolean[]) value);
		return Arrays.hashCode((Object[]) value);
	}

	@SuppressWarnings({"OverlyComplexMethod", "NeedBraces", "NPathComplexity", "CyclomaticComplexity"})
	private static boolean memberEquals(Object current, Object other) {
		Class<? extends @NonNull Object> valueClass = current.getClass();
		if (!valueClass.equals(other.getClass())) {
			return false;
		}
		if (!valueClass.isArray()) return current.equals(other);
		Class<? extends @Nullable Object> componentType = valueClass.getComponentType();
		if (componentType.equals(byte.class)) return Arrays.equals((byte[]) current, (byte[]) other);
		if (componentType.equals(char.class)) return Arrays.equals((char[]) current, (char[]) other);
		if (componentType.equals(double.class)) return Arrays.equals((double[]) current, (double[]) other);
		if (componentType.equals(float.class)) return Arrays.equals((float[]) current, (float[]) other);
		if (componentType.equals(int.class)) return Arrays.equals((int[]) current, (int[]) other);
		if (componentType.equals(long.class)) return Arrays.equals((long[]) current, (long[]) other);
		if (componentType.equals(short.class)) return Arrays.equals((short[]) current, (short[]) other);
		if (componentType.equals(boolean.class)) return Arrays.equals((boolean[]) current, (boolean[]) other);
		return Arrays.equals((Object[]) current, (Object[]) other);
	}

	@SuppressWarnings({"OverlyComplexMethod", "NeedBraces", "NPathComplexity", "CyclomaticComplexity"})
	private static String formatValue(Object value) {
		if (value instanceof String) return String.format("\"%s\"", value);
		if (value instanceof byte[]) return Arrays.toString((byte[]) value);
		if (value instanceof char[]) return Arrays.toString((char[]) value);
		if (value instanceof double[]) return Arrays.toString((double[]) value);
		if (value instanceof float[]) return Arrays.toString((float[]) value);
		if (value instanceof int[]) return Arrays.toString((int[]) value);
		if (value instanceof long[]) return Arrays.toString((long[]) value);
		if (value instanceof short[]) return Arrays.toString((short[]) value);
		if (value instanceof boolean[]) return Arrays.toString((boolean[]) value);
		if (value instanceof Object[]) return arrayFormat((Object[]) value);
		return value.toString();
	}

	@SuppressWarnings("MultipleStringLiterals")
	private static String arrayFormat(Object[] value) {
		StringBuilder builder = new StringBuilder("{");
		boolean written = false;
		for (Object element : value) {
			if (written) {
				builder.append(", ");
			}
			written = true;
			builder.append(formatValue(element));
		}
		builder.append("}");
		return builder.toString();
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
