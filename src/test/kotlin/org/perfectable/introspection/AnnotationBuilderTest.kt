package org.perfectable.introspection

import java.lang.IllegalArgumentException
import kotlin.reflect.full.isSubclassOf
import kotlin.test.*

class AnnotationBuilderKtTest {
    @Test
    fun `Annotation marker instance valid`() {
        val instance = Override::class.instance
        assertNotNull(instance)
    }

    @Test
    fun `Annotation marker instance with members`() {
        assertFailsWith<IllegalArgumentException>() {
            Deprecated::class.instance
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Annotation builder works`() {
        val message = "testMessage"
        val deprecationLevel = DeprecationLevel.WARNING
        val replaceWith = ReplaceWith::class.new {
	        member(ReplaceWith::expression, "expression")
	        member(ReplaceWith::imports, arrayOf("imports"))
        }
        val instance = Deprecated::class.new {
	        member(Deprecated::level, deprecationLevel)
	        member(Deprecated::message, "testMessage")
	        member(Deprecated::replaceWith, replaceWith)
        }
        assertNotNull(instance)
        assertEquals(deprecationLevel, instance.level)
        assertEquals(message, instance.message)
        assertEquals(replaceWith, instance.replaceWith)

	    val real = Marker::class.annotations[0]
	    assertEquals(real, instance)
	    assertEquals(real.hashCode(), instance.hashCode())
	    // toString is not checked, as kotlin does not list members in declaration order
	    // this is not needed as string representation is not standarized
    }

	@Deprecated(message = "testMessage", level = DeprecationLevel.WARNING,
		replaceWith = ReplaceWith(expression = "expression", imports = arrayOf("imports")))
	class Marker
}
