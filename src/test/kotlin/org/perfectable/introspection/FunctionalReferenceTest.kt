package org.perfectable.introspection

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.*

class FunctionalReferenceKtTest {
    @Test
    fun `Static reference`() {
        val instance: (Any) -> Int = System::identityHashCode
        val introspection = instance.introspect()
        assertEquals(Int::class.java, introspection.resultType())
        assertEquals(1, introspection.parametersCount())
        assertEquals(Object::class.java, introspection.parameterType(0))
        assertEquals(System::class.java, introspection.capturingType())
        assertEquals(introspection.referencedMethod(),
                System::class.staticFunctions.single { it.name == "identityHashCode" }.javaMethod)
    }

    @Test
    fun `Unbound property reference`() {
        val instance: (String) -> Int = String::length
        val introspection = instance.introspect()
        assertEquals(Int::class.java, introspection.resultType())
        assertEquals(1, introspection.parametersCount())
        assertEquals(String::class.java, introspection.parameterType(0))
        assertEquals(String::class.java, introspection.capturingType())
        assertEquals(introspection.referencedMethod(),
                String::class.memberProperties.single { it.name == "length" }.getter.javaMethod)
    }

    @Test
    fun `Bound property reference`() {
        val instance: (Any) -> String = "qwe"::plus
        val introspection = instance.introspect()
        assertEquals(Int::class.java, introspection.resultType())
        assertEquals(1, introspection.parametersCount())
        assertEquals(Object::class.java, introspection.parameterType(0))
        assertEquals(System::class.java, introspection.capturingType())
        assertEquals(introspection.referencedMethod(),
                System::class.staticFunctions.single { it.name == "identityHashCode" }.javaMethod)
    }

}
