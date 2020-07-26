package org.perfectable.introspection.type

import java.lang.reflect.*
import kotlin.reflect.KClass

val Type.typeView: TypeView get() = TypeView.of(this)
val <T> Class<T>.typeView get() = ClassView.of(this)
val <D : GenericDeclaration> TypeVariable<D>.typeView get() = TypeVariableView.of(this)
val GenericArrayType.typeView get() = ArrayTypeView.of(this)
val WildcardType.typeView get() = WildcardTypeView.of(this)

val <T: Any> KClass<T>.typeView get() = java.typeView
inline fun <reified T> typeView() = T::class.java.typeView

val <T> ClassView<T>.parameters get() = parameters()

val <D : GenericDeclaration> TypeVariableView<D>.upperBounds get() = upperBounds()

val WildcardTypeView.upperBounds get() = upperBounds()
val WildcardTypeView.lowerBounds get() = lowerBounds()

infix fun <T> T.instanceOf(view: TypeView) = view.isInstance(this)

infix fun Type.subtypeOf(other: Type) = this.typeView.isSubTypeOf(other.typeView)
infix fun Type.subtypeOf(other: KClass<*>) = this.typeView.isSubTypeOf(other.typeView)
infix fun Type.subtypeOf(view: TypeView) = typeView.isSubTypeOf(view)
infix fun TypeView.subtypeOf(other: Type) = this.isSubTypeOf(other)
infix fun TypeView.subtypeOf(other: KClass<*>) = this.isSubTypeOf(other.java)
infix fun TypeView.subtypeOf(view: TypeView) = this.isSubTypeOf(view)
infix fun KClass<*>.subtypeOf(type: Type) = this.java.subtypeOf(type.typeView)
infix fun KClass<*>.subtypeOf(other: KClass<*>) = this.java.typeView.isSubTypeOf(other.typeView)
infix fun KClass<*>.subtypeOf(view: TypeView) = this.java.typeView.isSubTypeOf(view)
