package org.perfectable.introspection


import spock.lang.Specification

import javax.annotation.Nonnull
import javax.annotation.meta.When

class FunctionalReferenceSpec extends Specification {

    def "should introspect basic static reference with no parameters"() {
        given:
        def referenceMarker = System::currentTimeMillis as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = System.getDeclaredMethod("currentTimeMillis")

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == System
            resultType() == long
            parametersCount() == 0
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitStatic(expectedMethod)
    }

    def "should introspect basic static reference with multiple parameters"() {
        given:
        def referenceMarker = System::getProperty as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = System.getDeclaredMethod("getProperty", String, String)

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == System
            resultType() == String
            parametersCount() == 2
            parameterType(0) == String
            parameterType(1) == String
            parameterAnnotations(0) == [] as Set
            parameterAnnotations(1) == [] as Set
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitStatic(expectedMethod)
    }

    def "should introspect custom static reference"() {
        given:
        def referenceMarker = FunctionalReferenceSpec::customStaticMethod as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = FunctionalReferenceSpec.getDeclaredMethod("customStaticMethod", IOException, Class)

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == FunctionalReferenceSpec
            resultType() == Serializable
            parametersCount() == 2
            parameterType(0) == IOException
            parameterType(1) == Class
            parameterAnnotations(0).size() == 1
            with(parameterAnnotations(0)[0]) {
                it.annotationType() == Nonnull
                (it as Nonnull).when() == When.MAYBE
            }
            parameterAnnotations(1).size() == 1
            with(parameterAnnotations(1)[0]) {
                it.annotationType() == Deprecated
            }
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitStatic(expectedMethod)
    }

    def "should introspect basic instance reference"() {
        given:
        def referenceMarker = String::length as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = String.getDeclaredMethod("length")

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == String
            resultType() == int
            parametersCount() == 1
            parameterType(0) == String
            parameterAnnotations(0).empty
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitInstance(expectedMethod)
    }

    def "should introspect custom instance reference"() {
        given:
        def referenceMarker = Example::customInstanceMethod as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = Example.getDeclaredMethod("customInstanceMethod", Throwable)

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == Example
            resultType() == SuppressWarnings
            parametersCount() == 2
            parameterType(0) == Example
            parameterType(1) == Throwable
            parameterAnnotations(0).empty
            parameterAnnotations(1).size() == 1
            with(parameterAnnotations(1)[0]) {
                it.annotationType() == Nonnull
                (it as Nonnull).when() == When.MAYBE
            }
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitInstance(expectedMethod)
    }

    def "should introspect basic bound reference"() {
        given:
        def bound = "value"
        def referenceMarker = bound::length as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = String.getDeclaredMethod("length")

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == String
            resultType() == int
            parametersCount() == 0
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitBound(expectedMethod, bound)
    }

    def "should introspect custom bound reference"() {
        given:
        def bound = new Example()
        def referenceMarker = bound::customInstanceMethod as FunctionalReference
        def visitor = Mock(FunctionalReference.Visitor)
        def expectedMethod = Example.getDeclaredMethod("customInstanceMethod", Throwable)

        when:
        def introspection = referenceMarker.introspect()

        then:
        with(introspection) {
            referencedMethod() == expectedMethod
            capturingType() == Example
            resultType() == SuppressWarnings
            parametersCount() == 1
            parameterType(0) == Throwable
            parameterAnnotations(0).size() == 1
            with(parameterAnnotations(0)[0]) {
                it.annotationType() == Nonnull
                (it as Nonnull).when() == When.MAYBE
            }
        }

        when:
        introspection.visit(visitor)

        then:
        1 * visitor.visitBound(expectedMethod, bound)
    }

    static Serializable customStaticMethod(@Nonnull(when = When.MAYBE) IOException exception,
                                     @Deprecated Class classParameter) {
        throw new AssertionError("This method should not be called")
    }

    static class Example {
        SuppressWarnings customInstanceMethod(@Nonnull(when = When.MAYBE) Throwable throwable) {
            throw new AssertionError("This method should not be called")
        }
    }


}
