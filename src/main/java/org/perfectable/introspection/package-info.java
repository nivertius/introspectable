/**
 * Base package for this library.
 *
 * <p>Contains miscellaneous utilities to use by frameworks.
 */
@DefaultQualifier(value = NonNull.class, locations = {
	TypeUseLocation.FIELD,
	TypeUseLocation.RETURN,
	TypeUseLocation.RECEIVER,
	TypeUseLocation.PARAMETER,
	TypeUseLocation.EXCEPTION_PARAMETER,
	TypeUseLocation.CONSTRUCTOR_RESULT,
	TypeUseLocation.RESOURCE_VARIABLE,
	TypeUseLocation.LOCAL_VARIABLE,
	TypeUseLocation.IMPLICIT_LOWER_BOUND
})
package org.perfectable.introspection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;
