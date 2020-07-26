package org.perfectable.introspection

fun <R> (() -> R).introspect(): FunctionalReference.Introspection =
        FunctionalReferenceIntrospection.ofKotlin(this)
fun <P1, R> ((P1) -> R).introspect(): FunctionalReference.Introspection =
        FunctionalReferenceIntrospection.ofKotlin(this)

