package org.perfectable.introspection.proxy

import java.lang.reflect.Method
import kotlin.reflect.KClass

fun <T> Class<T>.proxyBuilder() = ProxyBuilder.forType(this)
fun <T: Any> KClass<T>.proxyBuilder() = this.java.proxyBuilder()
inline fun <reified T: Any> proxyBuilder() = T::class.proxyBuilder()

fun <T: Any> Method.invocation(target: T) = MethodInvocation.of(this, target)
