package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

final class StandardQuery<T> implements Query<T> {
	private final Class<T> type;
	private final AnnotationMatch qualifiers;

	private StandardQuery(Class<T> type, AnnotationMatch qualifiers) {
		this.type = type;
		this.qualifiers = qualifiers;
	}

	static <T> StandardQuery<T> create(Class<T> type) {
		return new StandardQuery<>(type, AnnotationMatch.ANY);
	}

	@Override
	public StandardQuery<T> qualifiedWith(Annotation qualifier) {
		AnnotationMatch newQualifiers = qualifiers.add(AnnotationMatch.exact(qualifier));
		return new StandardQuery<>(type, newQualifiers);
	}

	@Override
	public StandardQuery<T> qualifiedWith(Class<? extends Annotation> qualifier) {
		AnnotationMatch newQualifiers = qualifiers.add(AnnotationMatch.typed(qualifier));
		return new StandardQuery<>(type, newQualifiers);
	}

	@Override
	public boolean matches(Class<?> targetClass, Set<Annotation> annotations) {
		return type.isAssignableFrom(targetClass)
			&& qualifiers.matchesAll(annotations);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", type.getName())
			.add("qualifiers", qualifiers)
			.toString();
	}

	private interface AnnotationMatch {
		AnnotationMatch ANY = new AnnotationMatch() {
			@Override
			public boolean matchesAll(Set<Annotation> annotations) {
				return true;
			}

			@Override
			public AnnotationMatch add(AnnotationMatch other) {
				return other;
			}

			@Override
			public String toString() {
				return "ANY";
			}
		};

		static AnnotationMatch typed(Class<? extends Annotation> qualifierType) {
			return new Typed(qualifierType);
		}

		static AnnotationMatch exact(Annotation qualifier) {
			return new Exact(qualifier);
		}

		boolean matchesAll(Set<Annotation> annotations);

		default AnnotationMatch add(AnnotationMatch other) {
			return Composite.create(this, other);
		}

		class Composite implements AnnotationMatch {
			private final ImmutableSet<AnnotationMatch> components;

			public static Composite create(AnnotationMatch... components) {
				return new Composite(ImmutableSet.copyOf(components));
			}

			private Composite(ImmutableSet<AnnotationMatch> components) {
				this.components = components;
			}

			@Override
			public boolean matchesAll(Set<Annotation> annotations) {
				return components.stream()
					.allMatch(component -> component.matchesAll(annotations));
			}

			@Override
			public AnnotationMatch add(AnnotationMatch other) {
				if (other == ANY) { // SUPPRESS CompareObjectsWithEquals
					return this;
				}
				ImmutableSet.Builder<AnnotationMatch> newComponents = ImmutableSet.<AnnotationMatch>builder()
					.addAll(components);
				if (other instanceof Composite) {
					ImmutableSet<AnnotationMatch> otherComponents = ((Composite) other).components;
					newComponents.addAll(otherComponents);
				}
				else {
					newComponents.add(other);
				}
				return new Composite(newComponents.build());
			}

			@Override
			public String toString() {
				return MoreObjects.toStringHelper(this)
					.add("components", components)
					.toString();
			}
		}

		class Typed implements AnnotationMatch {
			private final Class<? extends Annotation> qualifierType;

			Typed(Class<? extends Annotation> qualifierType) {
				this.qualifierType = qualifierType;
			}

			@Override
			public boolean matchesAll(Set<Annotation> annotations) {
				return annotations.stream()
					.map(Annotation::annotationType)
					.anyMatch(qualifierType::equals);
			}

			@Override
			public String toString() {
				return MoreObjects.toStringHelper(this)
					.add("qualifierType", qualifierType)
					.toString();
			}
		}

		class Exact implements AnnotationMatch {
			private final Annotation qualifier;

			Exact(Annotation qualifier) {
				this.qualifier = qualifier;
			}

			@Override
			public boolean matchesAll(Set<Annotation> annotations) {
				return annotations.contains(qualifier);
			}

			@Override
			public String toString() {
				return MoreObjects.toStringHelper(this)
					.add("qualifier", qualifier)
					.toString();
			}
		}
	}
}
