package org.perfectable.introspection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Annotation understood by Kotlin compiler for replacing platform types with non-null ones.
 *
 * <p>This annotation is needed because there's no default or popular annotation that would apply to anything else than
 * parameters. For parameters there is {@link javax.annotation.ParametersAreNonnullByDefault}, but this is not enough,
 * as for example it does not mark return types.
 *
 * <p>WARNING: This annotation was created only for internal usage in all subpackages, and it can be replaced with
 * official one at any time. You should not use this in your code.
 */
@Nonnull
@TypeQualifierDefault({
	ElementType.ANNOTATION_TYPE,
	ElementType.CONSTRUCTOR,
	ElementType.FIELD,
	ElementType.LOCAL_VARIABLE,
	ElementType.METHOD,
	ElementType.PACKAGE,
	ElementType.PARAMETER,
	ElementType.TYPE
})
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypesAreNotNullableByDefault {
	// nothing
}
