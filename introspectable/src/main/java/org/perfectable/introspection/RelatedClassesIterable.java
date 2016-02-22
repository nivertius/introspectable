package org.perfectable.introspection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RelatedClassesIterable extends MappingIterable.Unique<Class<?>> {
	private final Class<?> initial;
	private final Predicate<Class<?>> inclusionPredicate;
	
	public static RelatedClassesIterable of(Class<?> initial) {
		return new RelatedClassesIterable(initial, tested -> true);
	}
	
	private RelatedClassesIterable(Class<?> initial, Predicate<Class<?>> inclusionPredicate) {
		this.initial = initial;
		this.inclusionPredicate = inclusionPredicate;
	}
	
	@Override
	protected Collection<Class<?>> seed() {
		return map(this.initial);
	}
	
	public RelatedClassesIterable excludingPackage(Package newExcludedPackage) {
		checkNotNull(newExcludedPackage);
		return filter(tested -> !newExcludedPackage.equals(tested.getPackage()));
	}
	
	public RelatedClassesIterable excludingPrimitives() {
		return filter(tested -> !tested.isPrimitive());
	}
	
	public RelatedClassesIterable excludingInterfaces() {
		return filter(tested -> !tested.isInterface());
	}
	
	public RelatedClassesIterable excluding(Class<?> excludedClass) {
		checkNotNull(excludedClass);
		return filter(tested -> !excludedClass.equals(tested));
	}
	
	private RelatedClassesIterable filter(Predicate<Class<?>> additionalInclusionPredicate) {
		return new RelatedClassesIterable(this.initial, this.inclusionPredicate.and(additionalInclusionPredicate));
	}
	
	@Override
	protected Collection<Class<?>> map(Class<?> current) {
		Stream.Builder<Class<?>> resultBuilder = Stream.<Class<?>> builder();
		extractEnclosingClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		extractNestedClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		extractFieldClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		extractMethodClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		extractParameterClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		extractSuperClasses(current).filter(this.inclusionPredicate).forEach(resultBuilder::add);
		Set<Class<?>> result = resultBuilder.build().collect(Collectors.toSet());
		return result;
	}
	
	private static Stream<Class<?>> extractSuperClasses(Class<?> current) {
		// MARK this should work on generic superclass
		Stream.Builder<Class<?>> resultBuilder = Stream.builder();
		if(current.getSuperclass() != null) {
			resultBuilder.add(current.getSuperclass());
		}
		Stream.of(current.getInterfaces()).forEach(resultBuilder::add);
		return resultBuilder.build();
	}
	
	private static Stream<Class<?>> extractEnclosingClasses(Class<?> current) {
		final Class<?> enclosingClass = current.getEnclosingClass();
		if(enclosingClass == null) {
			return Stream.empty();
		}
		return Stream.of(enclosingClass);
	}
	
	private static Stream<Class<?>> extractNestedClasses(Class<?> current) {
		return Stream.of(current.getClasses());
	}
	
	private static Stream<Class<?>> extractFieldClasses(Class<?> current) {
		return Stream.of(current.getDeclaredFields())
				.map(Field::getType);
	}
	
	private static Stream<Class<?>> extractMethodClasses(Class<?> current) {
		// MARK this should work on generic parameter and return types
		return Stream.of(current.getDeclaredMethods())
				.flatMap(method -> Stream.concat(
						Stream.concat(
								Stream.of(method.getReturnType()),
								Stream.of(method.getParameterTypes())),
						Stream.of(method.getExceptionTypes())
						)
				);
	}
	
	private static Stream<Class<?>> extractParameterClasses(Class<?> current) {
		return Stream.of(current.getTypeParameters())
				.flatMap(variable -> Stream.of(variable.getBounds()))
				.filter(type -> type instanceof Class)
				.map(type -> (Class<?>) type);
	}
	
}
