package org.perfectable.introspection.query

import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass


val <T: AnnotatedElement> Class<T>.annotationQuery get() = AnnotationQuery.of(this)
val <T: AnnotatedElement> KClass<T>.annotationQuery get() = this.java.annotationQuery
inline fun <reified T: AnnotatedElement> annotationQuery() = T::class.java.annotationQuery

val ClassLoader.classQuery get() = ClassQuery.of(this)

val <T> Class<T>.constructorQuery get() = ConstructorQuery.of(this)
val <T: Any> KClass<T>.constructorQuery get() = this.java.constructorQuery
inline fun <reified T> constructorQuery() = T::class.java.constructorQuery

val <T> Class<T>.fieldQuery get() = FieldQuery.of(this)
val <T: Any> KClass<T>.fieldQuery get() = this.java.fieldQuery
inline fun <reified T> fieldQuery() = T::class.java.fieldQuery

val <T> Class<T>.inheritanceQuery get() = InheritanceQuery.of(this)
val <T: Any> KClass<T>.inheritanceQuery get() = this.java.inheritanceQuery
inline fun <reified T> inheritanceQuery() = T::class.java.inheritanceQuery

val <T> Class<T>.methodQuery get() = MethodQuery.of(this)
val <T: Any> KClass<T>.methodQuery get() = this.java.methodQuery
inline fun <reified T> methodQuery() = T::class.java.methodQuery
