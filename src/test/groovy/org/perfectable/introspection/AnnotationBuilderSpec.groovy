package org.perfectable.introspection

import java.lang.annotation.Annotation

import spock.lang.Specification

import org.perfectable.introspection.AnnotationBuilderExamples.Marker
import org.perfectable.introspection.AnnotationBuilderExamples.MarkerHolder
import org.perfectable.introspection.AnnotationBuilderExamples.Single
import org.perfectable.introspection.AnnotationBuilderExamples.SingleHolder
import org.perfectable.introspection.AnnotationBuilderExamples.Multiple
import org.perfectable.introspection.AnnotationBuilderExamples.MultipleHolder
import org.perfectable.introspection.AnnotationBuilderExamples.ArrayElements
import org.perfectable.introspection.AnnotationBuilderExamples.ArrayElementsHolder

@SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
class AnnotationBuilderSpec extends Specification {

    def "should reject non-interface class"() {
        when:
        AnnotationBuilder.marker(String as Class<Annotation>)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Provided class is not an annotation interface"
    }

    def "should reject non-annotation interface"() {
        when:
        AnnotationBuilder.marker(Serializable as Class<Annotation>)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Provided class is not an annotation interface"
    }

    def "should reject non-marker annotation"() {
        when:
        AnnotationBuilder.marker(Single)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Annotation interface is not a marker"
    }

    def "should reject null value for annotation member"() {
        given:
        def builder = AnnotationBuilder.of(Single)

        when:
        builder.with(Single::value, null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Null cannot be provided for member value of type class java.lang.String"
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    def "should reject invalid value type for annotation member"() {
        given:
        def builder = AnnotationBuilder.of(Single)
        def extractor = Single::value
        def fakeValue = AnnotationBuilderSpec

        when:
        builder.with(extractor, fakeValue)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Value ${fakeValue} (${fakeValue.class}) cannot be provided for member value of type class java.lang.String"
    }

    def "marker annotation should have correct behavior"() {
        given:
        def annotation = AnnotationBuilder.marker(Marker)

        expect:
        annotation instanceof Marker
        annotation.annotationType() == Marker
        annotation == MarkerHolder.HELD
        annotation != SingleHolder.HELD
        annotation != MultipleHolder.HELD
        annotation != new Object()
        annotation != null
        annotation != 1
        annotation == annotation
        MarkerHolder.HELD == annotation
    }

    def "marker annotation should have consistent hashCode"() {
        given:
        def annotation = AnnotationBuilder.marker(Marker)

        expect:
        annotation.hashCode() == 0
        annotation.hashCode() == MarkerHolder.HELD.hashCode()
    }

    def "marker annotation should have correct toString"() {
        given:
        def annotation = AnnotationBuilder.marker(Marker)

        expect:
        annotation.toString() == "@${Marker.getName()}()"
        annotation.toString() == MarkerHolder.HELD.toString()
    }

    def "should reject building Single annotation without value"() {
        given:
        def builder = AnnotationBuilder.of(Single)

        when:
        builder.build()

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "No value set for member 'value'"
    }

    def "Single annotation should have correct behavior"() {
        given:
        def annotation = AnnotationBuilder.of(Single)
                .with(Single::value, SingleHolder.VALUE)
                .build()

        expect:
        annotation instanceof Single
        annotation.annotationType() == Single
        annotation == SingleHolder.HELD
        annotation != new Object()
        annotation != null
        annotation != 1
        annotation == annotation
        SingleHolder.HELD == annotation
        annotation.value() == SingleHolder.VALUE
    }

    def "Single annotation should have correct hashCode"() {
        given:
        def annotation = AnnotationBuilder.of(Single)
                .with(Single::value, SingleHolder.VALUE)
                .build()
        def expectedHashCode = 127 * "value".hashCode() ^ SingleHolder.VALUE.hashCode()

        expect:
        annotation.hashCode() == expectedHashCode
        annotation.hashCode() == SingleHolder.HELD.hashCode()
    }

    def "Single annotation should have correct toString"() {
        given:
        def annotation = AnnotationBuilder.of(Single)
                .with(Single::value, SingleHolder.VALUE)
                .build()

        expect:
        annotation.toString() == "@${Single.getName()}(value=\"testValue\")"
    }

    def "Multiple annotation should have correct behavior"() {
        given:
        def annotation = AnnotationBuilder.of(Multiple)
                .with(Multiple::one, Multiple.DEFAULT_ONE)
                .with(Multiple::two, MultipleHolder.TWO_VALUE)
                .with(Multiple::three, MultipleHolder.TRHEE_VALUE)
                .build()

        expect:
        annotation instanceof Multiple
        annotation.annotationType() == Multiple
        annotation == MultipleHolder.HELD
        annotation != MarkerHolder.HELD
        annotation != SingleHolder.HELD
        annotation != new Object()
        annotation != null
        annotation != 1
        annotation == annotation
        MultipleHolder.HELD == annotation
        annotation.one() == Multiple.DEFAULT_ONE
        annotation.two() == MultipleHolder.TWO_VALUE
        annotation.three() == MultipleHolder.TRHEE_VALUE
    }

    def "Multiple annotation should have correct hashCode"() {
        given:
        def annotation = AnnotationBuilder.of(Multiple)
                .with(Multiple::one, Multiple.DEFAULT_ONE)
                .with(Multiple::two, MultipleHolder.TWO_VALUE)
                .with(Multiple::three, MultipleHolder.TRHEE_VALUE)
                .build()
        def expectedHashCode = (127 * "one".hashCode() ^ Multiple.DEFAULT_ONE.hashCode()) +
                (127 * "two".hashCode() ^ MultipleHolder.TWO_VALUE.hashCode()) +
                (127 * "three".hashCode() ^ Integer.valueOf(MultipleHolder.TRHEE_VALUE).hashCode())

        expect:
        annotation.hashCode() == expectedHashCode
        annotation.hashCode() == MultipleHolder.HELD.hashCode()
    }

    def "Multiple annotation should have correct toString"() {
        given:
        def annotation = AnnotationBuilder.of(Multiple)
                .with(Multiple::one, Multiple.DEFAULT_ONE)
                .with(Multiple::two, MultipleHolder.TWO_VALUE)
                .with(Multiple::three, MultipleHolder.TRHEE_VALUE)
                .build()

        expect:
        annotation.toString() == "@${Multiple.getName()}(one=\"defaultOne\", three=3, two=\"testValue\")"
    }

    def "ArrayElements annotation should have correct behavior"() {
        given:
        def annotation = AnnotationBuilder.of(ArrayElements)
                .with(ArrayElements::value, ["one", "two", "three"] as String[])
                .build()

        expect:
        annotation instanceof ArrayElements
        annotation.annotationType() == ArrayElements
        annotation == ArrayElementsHolder.HELD
        annotation != new Object()
        annotation != null
        annotation != 1
        annotation == annotation
        ArrayElementsHolder.HELD == annotation
        annotation.value() == ["one", "two", "three"] as String[]
    }

    def "ArrayElements annotation should have correct hashCode"() {
        given:
        def annotation = AnnotationBuilder.of(ArrayElements)
                .with(ArrayElements::value, ["one", "two", "three"] as String[])
                .build()
        def expectedHashCode = 127 * "value".hashCode() ^ Arrays.hashCode(["one", "two", "three"] as String[])

        expect:
        annotation.hashCode() == expectedHashCode
        annotation.hashCode() == ArrayElementsHolder.HELD.hashCode()
    }

    def "ArrayElements annotation should have correct toString"() {
        given:
        def annotation = AnnotationBuilder.of(ArrayElements)
                .with(ArrayElements::value, ["one", "two", "three"] as String[])
                .build()

        expect:
        annotation.toString() == "@${ArrayElements.getName()}(value={\"one\", \"two\", \"three\"})"
    }


}
