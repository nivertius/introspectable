package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.List;

import com.google.common.collect.Lists;

final class CompositeTypeMatch implements TypeMatch {
	private final List<TypeMatch> components;

	private CompositeTypeMatch(List<TypeMatch> components) {
		this.components = components;
	}

	static CompositeTypeMatch create(Class<?> targetClass, Annotation... qualifiers) {
		SimpleTypeMatch<?> element = SimpleTypeMatch.create(targetClass, qualifiers);
		return new CompositeTypeMatch(Lists.newArrayList(element));
	}

	@Override
	public boolean matches(Class<?> type, Annotation... qualifiers) {
		return components.stream().anyMatch(component -> component.matches(type, qualifiers));
	}

	public void add(Class<?> injectableClass, Annotation... qualifiers) {
		SimpleTypeMatch<?> newComponent = SimpleTypeMatch.create(injectableClass, qualifiers);
		components.add(newComponent);
	}
}
