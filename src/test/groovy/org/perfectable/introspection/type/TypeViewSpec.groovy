package org.perfectable.introspection.type

import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Type
import java.util.function.Supplier

@Unroll
class TypeViewSpec extends Specification {
    def 'OfClass non-generic basic characteristics'() {
        setup:
        ClassView<CharSequence> view = TypeView.of(CharSequence)

        expect:
        view.erasure() == CharSequence
        view.parameters() == []
    }

    def 'OfClass subtype between CharSequence and #type'() {
        setup:
        ClassView<CharSequence> view = TypeView.of(CharSequence)

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type         | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        CharSequence | true      | true        | false           | false
        String       | false     | true        | false           | true
        Object       | true      | false       | true            | false
        Number       | false     | false       | false           | false
        Object[]     | false     | false       | false           | false
        Number[]     | false     | false       | false           | false
    }

    def 'OfClass generic'() {
        setup:
        ClassView<TypeExamples.Root<?>> view = TypeView.of(TypeExamples.Root)

        expect:
        view.erasure() == TypeExamples.Root
        view.parameters().size() == 1
        with (view.parameters()[0]) {
            def variable = it.asVariable()
            variable.erasure() == Object
            variable.upperBounds().collect { it.unwrap() } == [Object]
        }
    }

    def 'OfClass generic resolved with #resolver'() {
        setup:
        ClassView<TypeExamples.Root<?>> view = TypeView.of(TypeExamples.Root)
        def resolved = view.resolve(resolver)

        expect:
        resolved.erasure() == TypeExamples.Root
        resolved.arguments().size() == 1
        with (resolved.arguments()[0]) {
            def variable = it.asVariable()
            variable.erasure() == erasure
            variable.upperBounds().collect { it.unwrap() } == [erasure]
        }

        and:
        def parameter = view.parameters()[0].resolve(resolver)
        parameter.erasure() == erasure

        where:
        resolver               | erasure
        TypeExamples.Root      | Object
        String                 | Object
        TypeExamples.Unbounded | Number
    }

    def 'OfClass generic resolved to class'() {
        setup:
        ClassView<TypeExamples.Root<?>> view = TypeView.of(TypeExamples.Root)
        def result = view.resolve(TypeExamples.Bounded)

        expect:
        result.erasure() == TypeExamples.Root
        result.arguments().size() == 1
        with (result.arguments()[0]) {
            def variable = it.asClass()
            variable.erasure() == Long
        }
    }

    def 'OfClass generic double resolved'() {
        setup:
        ClassView<TypeExamples.Root<?>> view = TypeView.of(TypeExamples.Root)
        def result = view.resolve(TypeExamples.Unbounded).resolve(TypeExamples.Bounded)

        expect:
        result.erasure() == TypeExamples.Root
        result.arguments().size() == 1
        with (result.arguments()[0]) {
            def variable = it.asClass()
            variable.erasure() == Long
        }
    }

    def 'OfParameterized with constant basic characteristics'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_CONSTANT_FIELD).asParameterized()

        expect:
        view.erasure() == Supplier
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def argument = it.asClass()
            argument.erasure() == String
        }
    }

    def 'OfParameterized with constant subtype test with #type'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_CONSTANT_FIELD).asParameterized()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype


        where:
        type                        | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Supplier                    | true      | false       | true            | false
        Object                      | true      | false       | true            | false
        String                      | false     | false       | false           | false
        TypeExamples.StringSupplier | false     | true        | false           | true
    }

    def 'OfParameterized with variable unresolved basic characteristics'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_PARAMETER_FIELD).asParameterized()

        expect:
        view.erasure() == Supplier
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def argument = it.asVariable()
            it.unwrap() == TypeExamples.Unbounded.CLASS_FIRST_VARIABLE
            argument.erasure() == Number
            argument.upperBounds().collect { it.unwrap() } == [Number ]
        }
    }

    def 'OfParameterized with variable unresolved subtype test with #type'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_PARAMETER_FIELD).asParameterized()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                          | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Supplier                                      | true      | false       | true            | false
        Object                                        | true      | false       | true            | false
        String                                        | false     | false       | false           | false
        Number[]                                      | false     | false       | false           | false
        TypeExamples.StringSupplier                   | false     | false       | false           | false
        TypeExamples.Unbounded.FirstParameterSupplier | false     | true        | false           | true
    }

    def 'OfParameterized with variable resolved basic characteristics'() {
        setup:
        ParameterizedTypeView view = TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_PARAMETER_FIELD)
            .resolve(TypeExamples.Bounded)
            .asParameterized()

        expect:
        view.erasure() == Supplier
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def argument = it.asClass()
            argument.unwrap() == Long
        }
    }

    def 'OfParameterized with variable resolved subtype test with #type'() {
        setup:
        ParameterizedTypeView view = TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_WITH_PARAMETER_FIELD)
                .resolve(TypeExamples.Bounded)
                .asParameterized()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                          | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Supplier                                      | true      | false       | true            | false
        Object                                        | true      | false       | true            | false
        String                                        | false     | false       | false           | false
        Number[]                                      | false     | false       | false           | false
        TypeExamples.StringSupplier                   | false     | false       | false           | false
        TypeExamples.LongSupplier                     | false     | true        | false           | true
        TypeExamples.Unbounded.FirstParameterSupplier | false     | false       | false           | false
    }

    def 'OfParameterized with unbounded wildcard basic characteristics'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.NONCANONICAL_WILDCARD_FIELD).asParameterized();

        expect:
        view.erasure() == TypeExamples.Unbounded
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def argument = it.asWildcard()
            argument.erasure() == Number
            argument.lowerBounds().empty
            argument.upperBounds().collect { it.erasure() } == [ Number ]
        }
    }

    def 'OfParameterized with unbounded wildcard subtype test with #type'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.NONCANONICAL_WILDCARD_FIELD).asParameterized();

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                         | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                       | true      | false       | true            | false
        String                                       | false     | false       | false           | false
        Number[]                                      | false     | false       | false           | false
        TypeExamples.Root                            | true      | false       | true            | false
        TypeExamples.Unbounded                       | true      | true        | true            | true
        TypeExamples.Bounded                         | false     | true        | false           | true
        TypeExamples.ExternalizableUnbounded         | false     | true        | false           | true
        TypeExamples.ExternalizableTypeNumberBounded | false     | true        | false           | true
    }

    def 'OfParameterized with bounded wildcard basic characteristics'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.NONCANONICAL_BOUNDED_WILDCARD_FIELD).asParameterized();

        expect:
        view.erasure() == TypeExamples.Unbounded
        view.arguments().size() == 1
        with(view.arguments()[0]) {
            def argument = it.asWildcard()
            argument.erasure() == Number
            argument.lowerBounds().empty
            argument.upperBounds().collect { it.erasure() } == [Number, Externalizable]
        }
    }

    def 'OfParameterized with bounded wildcard subtype test with #type'() {
        setup:
        ParameterizedTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.NONCANONICAL_BOUNDED_WILDCARD_FIELD).asParameterized();

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                         | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                       | true      | false       | true            | false
        String                                       | false     | false       | false           | false
        TypeExamples.Root                            | true      | false       | true            | false
        TypeExamples.Unbounded                       | true      | false       | true            | false
        TypeExamples.Bounded                         | false     | false       | false           | false
        TypeExamples.ExternalizableUnbounded         | false     | true        | false           | true
        TypeExamples.ExternalizableTypeNumberBounded | false     | true        | false           | true
    }

    def 'OfVariable inherited unresolved basic characteristics'() {
        setup:
        TypeVariableView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0).asVariable();

        expect:
        view.unwrap() == TypeExamples.Root.CLASS_FIRST_VARIABLE
        view.erasure() == Object
        view.upperBounds().collect { it.unwrap() } == [Object ]
    }

    def 'OfVariable inherited unresolved subtype test with #type'() {
        setup:
        TypeVariableView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0).asVariable();

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                        | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        TypeExamples.Root.CLASS_FIRST_VARIABLE      | true      | true        | false           | false
        TypeExamples.Unbounded.CLASS_FIRST_VARIABLE | false     | false       | false           | false
        Object                                      | true      | false       | true            | false
        String                                      | false     | false       | false           | false
        Number                                      | false     | false       | false           | false
    }

    def 'OfVariable inherited resolved basic characteristics'() {
        setup:
        TypeVariableView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0)
                        .resolve(TypeExamples.Unbounded)
                        .asVariable()

        expect:
        view.unwrap() == TypeExamples.Unbounded.CLASS_FIRST_VARIABLE
        view.erasure() == Number
        view.upperBounds().collect { it.unwrap() } == [Number ]
    }

    def 'OfVariable inherited resolved subtype test with #type'() {
        setup:
        TypeVariableView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0)
                        .resolve(TypeExamples.Unbounded)
                        .asVariable()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                        | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        TypeExamples.Unbounded.CLASS_FIRST_VARIABLE | true      | true        | false           | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE      | false     | false       | false           | false
        Object                                      | true      | false       | true            | false
        Externalizable                              | false     | false       | false           | false
        Number                                      | true      | false       | true            | false
        Long                                        | false     | false       | false           | false
        String                                      | false     | false       | false           | false
    }

    def 'OfVariable inherited resolved total'() {
        setup:
        TypeView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0)
                        .resolve(TypeExamples.Bounded)

        expect:
        view.unwrap() == Long
    }

    def 'OfVariable inherited resolved twice'() {
        setup:
        TypeView view =
                TypeView.ofParameterOf(TypeExamples.Root.PARAMETER_WITH_GENERIC_TYPE_METHOD, 0)
                        .resolve(TypeExamples.Unbounded)
                        .resolve(TypeExamples.Bounded)

        expect:
        view.unwrap() == Long
    }

    def 'OfVariable multiple bounds basic characteristics'() {
        setup:
        TypeVariableView view =
                TypeView.ofTypeParameterOf(TypeExamples.Root.MULTIPLE_BOUNDS_METHOD, 0)

        expect:
        view.unwrap() == TypeExamples.Root.MULTIPLE_BOUNDS_METHOD_FIRST_VARIABLE
        view.erasure() == Number
        view.upperBounds().collect { it.unwrap() } == [Number, Externalizable, Type ]
    }

    def 'OfVariable multiple bounds subtype test with #type'() {
        setup:
        TypeVariableView view =
                TypeView.ofTypeParameterOf(TypeExamples.Root.MULTIPLE_BOUNDS_METHOD, 0)

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                  | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                | true      | false       | true            | false
        Type                                  | true      | false       | true            | false
        Externalizable                        | true      | false       | true            | false
        Serializable                          | true      | false       | true            | false
        TypeExamples.ExternalizableNumber     | false     | false       | false           | false
        TypeExamples.ExternalizableTypeNumber | false     | false       | false           | false
        Number                                | true      | false       | true            | false
        Long                                  | false     | false       | false           | false
        String                                | false     | false       | false           | false
        Number[]                              | false     | false       | false           | false
    }

    def 'OfVariable extends bounds unresolved basic characteristics'() {
        setup:
        TypeVariableView view =
                TypeView.ofTypeParameterOf(TypeExamples.Root.EXTENDS_TYPE_PARAMETER_METHOD, 0)

        expect:
        view.unwrap() == TypeExamples.Root.EXTENDS_TYPE_PARAMETER_METHOD_FIRST_VARIABLE
        view.erasure() == Object
        view.upperBounds().collect { it.unwrap() } == [TypeExamples.Root.CLASS_FIRST_VARIABLE ]
    }

    def 'OfVariable extends bounds unresolved subtype test with #type'() {
        setup:
        TypeVariableView view =
                TypeView.ofTypeParameterOf(TypeExamples.Root.EXTENDS_TYPE_PARAMETER_METHOD, 0)

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                   | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                 | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE | true      | false       | true            | false
        Number                                 | false     | false       | false           | false
        Long                                   | false     | false       | false           | false
        String                                 | false     | false       | false           | false
    }

    def 'OfVariable resolving variable'() {
        setup:
        TypeView view =
                TypeView.of(TypeExamples.Unbounded)
                        .resolve(TypeExamples.Unbounded.VARIABLE_RESOLVING_METHOD_FIRST_VARIABLE)
                        .asParameterized()

        expect:
        view.arguments().collect { it.unwrap() } == [ Double ]
    }

    def 'OfWildcard unbounded basic characteristics'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.UNBOUNDED_WILDCARD_FIELD).asParameterized()
                    .arguments()[0].asWildcard()

        expect:
        view.erasure() == Object
        view.lowerBounds().collect { it.unwrap() } == []
        view.upperBounds().collect { it.unwrap() } == [ Object ]
    }

    def 'OfWildcard unbounded subtype test with #type'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.UNBOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                   | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                 | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE | false | false | false | false
        Number                                 | false     | false       | false           | false
        String                                 | false     | false       | false           | false
    }


    def 'OfWildcard upper bounded basic characteristics'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.UPPER_BOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.erasure() == Number
        view.lowerBounds().collect { it.unwrap() } == []
        view.upperBounds().collect { it.unwrap() } == [ Number ]
    }

    def 'OfWildcard upper bounded subtype test with #type'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.UPPER_BOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                                               | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                                             | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE                             | false     | false       | false           | false
        Number                                                             | true      | false       | true            | false
        String                                                             | false     | false       | false           | false
        TypeExamples.Unbounded.UPPER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT | false     | false       | false           | false
        TypeExamples.Unbounded.LOWER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT | true      | false       | true            | false
    }

    def 'OfWildcard lower bounded basic characteristics'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.LOWER_BOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.erasure() == Object
        view.lowerBounds().collect { it.unwrap() } == [ Number ]
        view.upperBounds().collect { it.unwrap() } == [ Object ]
    }

    def 'OfWildcard lower bounded subtype test with #type'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.LOWER_BOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                                               | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                                             | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE                             | false     | false       | false           | false
        Number                                                             | false     | true        | false           | true
        String                                                             | false     | false       | false           | false
        TypeExamples.Unbounded.UPPER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT | false     | true        | false           | true
        TypeExamples.Unbounded.LOWER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT | false     | false       | false           | false
    }

    def 'OfWildcard bounded unresolved with variable basic characteristics'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.BOUNDED_VARIABLE_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.erasure() == Number
        view.lowerBounds().collect { it.unwrap() } == []
        view.upperBounds().collect { it.unwrap() } == [ TypeExamples.Unbounded.CLASS_FIRST_VARIABLE ]
    }

    def 'OfWildcard bounded unresolved with variable subtype test with #type'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.LOWER_BOUNDED_WILDCARD_FIELD).asParameterized()
                        .arguments()[0].asWildcard()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                   | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                 | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE | false     | false       | false           | false
        Number                                 | false     | true        | false           | true
        Long                                   | false     | true        | false           | true
        String                                 | false     | false       | false           | false
    }

    def 'OfWildcard bounded resolved with variable basic characteristics'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.BOUNDED_VARIABLE_WILDCARD_FIELD).asParameterized()
                        .arguments()[0]
                        .resolve(TypeExamples.Bounded)
                        .asWildcard()

        expect:
        view.erasure() == Long
        view.lowerBounds().collect { it.unwrap() } == []
        view.upperBounds().collect { it.unwrap() } == [ Long ]
    }

    def 'OfWildcard bounded resolved with variable subtype test with #type'() {
        setup:
        WildcardTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.BOUNDED_VARIABLE_WILDCARD_FIELD).asParameterized()
                        .arguments()[0]
                        .resolve(TypeExamples.Bounded)
                        .asWildcard()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type                                   | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object                                 | true      | false       | true            | false
        TypeExamples.Root.CLASS_FIRST_VARIABLE | false     | false       | false           | false
        Number                                 | true      | false       | true            | false
        Long                                   | true      | false       | true            | false
        String                                 | false     | false       | false           | false
    }

    def 'OfWildcard resolving variable'() {
        setup:
        TypeView view =
                TypeView.of(TypeExamples.Unbounded)
                        .resolve(TypeExamples.Unbounded.BOUNDED_WILDCARD_RESOLVING_FIELD_TYPE_FIRST_ARGUMENT)
                        .asParameterized()

        expect:
        view.arguments().collect { it.unwrap() } == [ Integer ]
    }

    def 'OfArray from class basic characteristics'() {
        setup:
        ArrayTypeView view = TypeView.of(Number[]).asArray()

        expect:
        view.erasure() == Number[]
        with(view.component()) {
            def component = it.asClass()
            component.unwrap() == Number
        }
    }

    def 'OfArray from class subtype test with #type'() {
        setup:
        ArrayTypeView view = TypeView.of(Number[]).asArray()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type      | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object    | true      | false       | true            | false
        Number    | false     | false       | false           | false
        String    | false     | false       | false           | false
        Object[]  | true      | false       | true            | false
        Number[]  | true      | true        | true            | true
        Long[]    | false     | true        | false           | true
    }

    def 'OfArray from class to class'() {
        setup:
        ClassView view = TypeView.of(Number[]).asClass().asArray().asClass()

        expect:
        view.asClass().unwrap() == Number[]
    }

    def 'OfArray as parameterized basic characteristics'() {
        setup:
        ParameterizedTypeView view = TypeView.of(Number[]).asParameterized()

        expect:
        view.erasure() == Number[]
    }

    def 'OfArray as parameterized subtype test with #type'() {
        setup:
        ParameterizedTypeView view = TypeView.of(Number[]).asParameterized()

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type      | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object    | true      | false       | true            | false
        Number    | false     | false       | false           | false
        String    | false     | false       | false           | false
        Object[]  | true      | false       | true            | false
        Number[]  | true      | true        | true            | true
        Long[]    | false     | true        | false           | true
    }

    def 'OfArray unresolved basic characteristics'() {
        setup:
        ArrayTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_ARRAY_FIELD).asArray();

        expect:
        view.erasure() == Number[]
        with(view.component()) {
            def component = it.asVariable()
            component.unwrap() == TypeExamples.Unbounded.CLASS_FIRST_VARIABLE
        }
    }

    def 'OfArray unresolved subtype test with #type'() {
        setup:
        ArrayTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_ARRAY_FIELD).asArray();

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type      | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object    | true      | false       | true            | false
        Number    | false     | false       | false           | false
        String    | false     | false       | false           | false
        Object[]  | true      | false       | true            | false
        Number[]  | true      | false       | true            | false
        Long[]    | false     | false       | false           | false
    }


    def 'OfArray resolved basic characteristics'() {
        setup:
        ArrayTypeView view = TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_ARRAY_FIELD).asArray()
                .resolve(TypeExamples.Bounded)

        expect:
        view.erasure() == Long[]
        with(view.component()) {
            def component = it.asClass()
            component.erasure() == Long
        }
    }

    def 'OfArray resolved subtype test with #type'() {
        setup:
        ArrayTypeView view =
                TypeView.ofTypeOf(TypeExamples.Unbounded.GENERIC_ARRAY_FIELD).asArray()
                        .resolve(TypeExamples.Bounded)

        expect:
        view.isSubTypeOf(type) == isSubtype
        view.isProperSubtypeOf(type) == isProperSubtype
        view.isSuperTypeOf(type) == isSuperType
        view.isProperSupertypeOf(type) == isProperSupertype

        where:
        type      | isSubtype | isSuperType | isProperSubtype | isProperSupertype
        Object    | true      | false       | true            | false
        Number    | false     | false       | false           | false
        String    | false     | false       | false           | false
        Object[]  | true      | false       | true            | false
        Number[]  | true      | false       | true            | false
        Long[]    | true      | true        | true            | true
    }
}

