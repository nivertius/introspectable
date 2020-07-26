package org.perfectable.introspection

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Instance of a member-less annotation from a java class.
 *
 * @return Instance of a marker annotation
 */
val <A: Annotation> Class<A>.instance get() = AnnotationBuilder.marker(this)

/**
 * Instance of a member-less annotation for a kotlin class.
 *
 * @return Instance of a marker annotation
 */
val <A: Annotation> KClass<A>.instance get() = this.java.instance

class AnnotationBuilderDSL<A: Annotation> internal constructor (private var builder: AnnotationBuilder<A>){
	fun <T: Any> member(extractor: (A) -> T, value: T) {
		builder = builder.with(extractor, value)
	}

	internal fun build() = builder.build()
}

/**
 * New builder for a annotation from a java class
 *
 * @return Fresh builder for a annotation
 */
fun <A: Annotation> Class<A>.new(config: AnnotationBuilderDSL<A>.() -> Unit): A {
	val builder = AnnotationBuilderDSL<A>(AnnotationBuilder.of(this))
	config(builder)
	return builder.build()
}

/**
 * New builder for a annotation from a kotlin class
 *
 * @return Fresh builder for a annotation
 */
fun <A: Annotation> KClass<A>.new(builder: AnnotationBuilderDSL<A>.() -> Unit) = this.java.new(builder)

