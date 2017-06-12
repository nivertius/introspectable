package org.perfectable.introspection.injection;

import com.google.common.collect.ImmutableList;

final class CompositeTypeMatch implements TypeMatch {
	private final ImmutableList<TypeMatch> components;

	private CompositeTypeMatch(ImmutableList<TypeMatch> components) {
		this.components = components;
	}

	static CompositeTypeMatch create(TypeMatch... matches) {
		return new CompositeTypeMatch(ImmutableList.copyOf(matches));
	}

	@Override
	public boolean matches(Query<?> query) {
		return components.stream().anyMatch(component -> component.matches(query));
	}

	@Override
	public TypeMatch orElse(TypeMatch other) {
		ImmutableList.Builder<TypeMatch> builder = ImmutableList.<TypeMatch>builder().addAll(components);
		if (other instanceof CompositeTypeMatch) {
			builder.addAll(((CompositeTypeMatch) other).components);
		}
		else {
			builder.add(other);
		}
		return new CompositeTypeMatch(builder.build());
	}
}
