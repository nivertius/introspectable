package org.perfectable.introspection.bean

import kotlin.reflect.KClass

fun <T: Any> T.bean() = Bean.from(this)
fun <T: Any> Class<T>.beanSchema() = BeanSchema.from(this)
fun <T: Any> KClass<T>.beanSchema() = this.java.beanSchema()
inline fun <reified T: Any> beanSchema() = T::class.beanSchema()

class BeanPropertyMap<B: Any>(private val bean: Bean<B>) {
    operator fun get(name: String) = bean.property(name)
}
val <B: Any> Bean<B>.properties: BeanPropertyMap<B> get() = BeanPropertyMap(this)

class BeanSchemaPropertyMap<B: Any>(private val bean: BeanSchema<B>) {
    operator fun get(name: String) = bean.property(name)
}
val <B: Any> BeanSchema<B>.properties: BeanSchemaPropertyMap<B> get() = BeanSchemaPropertyMap(this)

fun <B: Any, T, X: T> Property<B, T>.cast(newValueType: Class<X>) = this.`as`(newValueType)
