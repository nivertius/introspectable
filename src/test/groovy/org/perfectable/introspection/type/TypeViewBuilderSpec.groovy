package org.perfectable.introspection.type

import spock.lang.Specification

import java.lang.reflect.Method
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class TypeViewBuilderSpec extends Specification {
    def 'parameterizedTypeBuilder empty'() {
        setup:
        ParameterizedTypeView view =
                TypeView.of(TypeExamples.Root)
                        .parameterizedBuilder()
                        .build()

        expect:
        view.erasure() == TypeExamples.Root
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def variable = it.asVariable()
            variable.erasure() == Object
            variable.upperBounds().collect { it.unwrap() } == [Object]
        }
        with(view.unwrap()) {
            getRawType() == TypeExamples.Root
            getOwnerType() == TypeExamples
            getActualTypeArguments().size() == 1
            getActualTypeArguments()[0] instanceof TypeVariable
        }
    }

    def 'parameterizedTypeBuilder with replaced owner'() {
        setup:
        ParameterizedTypeView view =
                TypeView.of(TypeExamples.Root)
                        .parameterizedBuilder()
                        .withOwner(String)
                        .build()

        expect:
        view.erasure() == TypeExamples.Root
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def variable = it.asVariable()
            variable.erasure() == Object
            variable.upperBounds().collect { it.unwrap() } == [Object]
        }
        with(view.unwrap()) {
            getRawType() == TypeExamples.Root
            getOwnerType() == String
            getActualTypeArguments().size() == 1
            getActualTypeArguments()[0] instanceof TypeVariable
        }
    }

    def 'parameterizedTypeBuilder with substitution by index'() {
        setup:
        ParameterizedTypeView view =
                TypeView.of(TypeExamples.Root)
                        .parameterizedBuilder()
                        .withSubstitution(0, String)
                        .build()

        expect:
        view.erasure() == TypeExamples.Root
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def variable = it.asClass()
            variable.unwrap() == String
        }
        with(view.unwrap()) {
            getRawType() == TypeExamples.Root
            getOwnerType() == TypeExamples
            getActualTypeArguments() == [String] as Type[]
        }
    }

    def 'parameterizedTypeBuilder with substitution by invalid index'() {
        setup:
        ParameterizedTypeView.Builder builder = TypeView.of(TypeExamples.Root)
                .parameterizedBuilder()

        when:
        builder.withSubstitution(20, String)

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Type interface org.perfectable.introspection.type.TypeExamples\$Root has no parameter " +
                "with index 20"
    }


    def 'parameterizedTypeBuilder with substitution by outside bounds'() {
        setup:
        def builder = TypeView.of(TypeExamples.Unbounded).parameterizedBuilder()

        when:
        builder.withSubstitution(0, String)

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Substitute class java.lang.String is outside bound class java.lang.Number"
    }

    def 'parameterizedTypeBuilder with substitution by name'() {
        setup:
        ParameterizedTypeView view =
                TypeView.of(TypeExamples.Root)
                        .parameterizedBuilder()
                        .withSubstitution("X", Integer)
                        .build()

        expect:
        view.erasure() == TypeExamples.Root
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def variable = it.asClass()
            variable.unwrap() == Integer
        }
        with(view.unwrap()) {
            getRawType() == TypeExamples.Root
            getOwnerType() == TypeExamples
            getActualTypeArguments() == [Integer] as Type[]
        }
    }

    def 'parameterizedTypeBuilder with substitution by invalid name'() {
        setup:
        ParameterizedTypeView.Builder builder = TypeView.of(TypeExamples.Root)
                .parameterizedBuilder()

        when:
        builder.withSubstitution("INVALID", Integer)

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Type interface org.perfectable.introspection.type.TypeExamples\$Root has no parameter " +
                "with name INVALID"
    }

    def 'buildArray'() {
        setup:
        ArrayTypeView arrayType = TypeView.of(TypeExamples.Root).buildArray()

        expect:
        with(arrayType) {
            erasure().isArray()
            erasure().getComponentType() == TypeExamples.Root
        }
        with(arrayType.unwrap()) {
            getGenericComponentType() == TypeExamples.Root
            it.toString() == "org.perfectable.introspection.type.TypeExamples\$Root[]"
        }
    }

    def 'typeVariable empty'() {
        setup:
        TypeVariableView<Method> variable =
                TypeVariableView.builder("test", TypeExamples.Unbounded.VARIABLE_RESOLVING_METHOD)
                    .build();

        expect:
        with(variable) {
            erasure() == Object
            upperBounds().empty
        }
        def unwrapped = variable.unwrap()
        with(unwrapped) {
            getName() == "test"
            getGenericDeclaration() == TypeExamples.Unbounded.VARIABLE_RESOLVING_METHOD
            bounds.length == 0
            it.toString() == "test"
        }
    }

    def 'typeVariable with bounds'() {
        setup:
        TypeVariableView<Method> variable =
                TypeVariableView.builder("test", TypeExamples.Unbounded.VARIABLE_RESOLVING_METHOD)
                        .withUpperBound(Number)
                        .withUpperBound(Serializable)
                        .build();

        expect:
        with(variable) {
            erasure() == Number
            upperBounds() == [TypeView.of(Number), TypeView.of(Serializable)]
        }
        def unwrapped = variable.unwrap()
        with(unwrapped) {
            getName() == "test"
            getGenericDeclaration() == TypeExamples.Unbounded.VARIABLE_RESOLVING_METHOD
            bounds == [Number, Serializable] as Type[]
            it.toString() == "test"
        }
    }

    def 'wildcard unbounded'() {
        setup:
        WildcardTypeView variable = WildcardTypeView.unbounded()

        expect:
        with(variable) {
            erasure() == Object
            upperBounds() == []
            lowerBounds() == []
        }
        def unwrapped = variable.unwrap()
        with(unwrapped) {
            lowerBounds == [] as Type[]
            upperBounds == [] as Type[]
            it.toString() == "?"
        }
    }

    def 'wildcard with bounds'() {
        setup:
        WildcardTypeView variable = WildcardTypeView.builder()
            .withUpperBound(Number)
            .withUpperBound(Serializable)
            .withLowerBound(Integer)
            .build()

        expect:
        with(variable) {
            erasure() == Number
            upperBounds() == [TypeView.of(Number), TypeView.of(Serializable)]
            lowerBounds() == [TypeView.of(Integer)]
        }
        def unwrapped = variable.unwrap()
        with(unwrapped) {
            getUpperBounds() == [Number, Serializable] as Type[]
            getLowerBounds() == [Integer] as Type[]
            it.toString() == "? super java.lang.Integer extends java.lang.Number & java.io.Serializable"
        }
    }
}
