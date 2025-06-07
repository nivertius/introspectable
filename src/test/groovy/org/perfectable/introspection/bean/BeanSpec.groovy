package org.perfectable.introspection.bean

import spock.lang.Specification
import static org.perfectable.introspection.bean.BeanExamples.*

class BeanSpec extends Specification {
    public static final String NON_EXISTENT_PROPERTY_NAME = "nonExistentProperty"

    def "empty bean has no properties"() {
        given:
        def instance = new EmptySubject()
        
        when:
        def bean = Bean.from(instance)
        
        then:
        with(bean) {
            type == EmptySubject
            fieldProperties.empty
            related.empty
            !hasProperty(NON_EXISTENT_PROPERTY_NAME)
            contentsSameAs(instance)
            hasConsistentCopyMechanism()
        }
    }

    def "single field without value has expected properties"() {
        given:
        def instance = new SingleFieldSubject()
        
        when:
        def bean = Bean.from(instance)
        
        then:
        with(bean) {
            type == SingleFieldSubject
            fieldProperties.collect { it.name } == [SingleFieldSubject.PROPERTY_ONE_NAME]
            related.empty
            !hasProperty(NON_EXISTENT_PROPERTY_NAME)
            contentsSameAs(instance)
            hasConsistentCopyMechanism()
        }
    }

    def "single field with value has expected properties and value"() {
        given:
        def instance = new SingleFieldSubject()
        def oneValue = "testValue"
        instance.one = oneValue
        
        when:
        def bean = Bean.from(instance)
        
        then:
        with(bean) {
            type == SingleFieldSubject
            fieldProperties.collect { it.name } == [SingleFieldSubject.PROPERTY_ONE_NAME]
            related == [oneValue]
            !hasProperty(NON_EXISTENT_PROPERTY_NAME)
            contentsSameAs(instance)
            hasConsistentCopyMechanism()
        }
        
        and: "property details are correct"
        with(bean.property(SingleFieldSubject.PROPERTY_ONE_NAME)) {
            name == SingleFieldSubject.PROPERTY_ONE_NAME
            type == String
            writable
            readable
            value == oneValue
        }
    }

    def "single getter has expected properties"() {
        given:
        def instance = new SingleGetterSubject()
        
        when:
        def bean = Bean.from(instance)
        
        then:
        with(bean) {
            type == SingleGetterSubject
            fieldProperties.empty
            related.empty
            !hasProperty(NON_EXISTENT_PROPERTY_NAME)
            contentsSameAs(instance)
            hasConsistentCopyMechanism()
        }
        
        and: "property details are correct"
        with(bean.property(SingleGetterSubject.PROPERTY_ONE_NAME)) {
            name == SingleGetterSubject.PROPERTY_ONE_NAME
            type == String
            !writable
            readable
            value == SingleGetterSubject.VALUE_ONE
        }
    }

    def "single setter has expected properties"() {
        given:
        def instance = new SingleSetterSubject()
        
        when:
        def bean = Bean.from(instance)
        
        then:
        with(bean) {
            type == SingleSetterSubject
            fieldProperties.empty
            related.empty
            !hasProperty(NON_EXISTENT_PROPERTY_NAME)
            contentsSameAs(instance)
            hasConsistentCopyMechanism()
        }
        
        and: "property details are correct"
        with(bean.property(SingleSetterSubject.PROPERTY_ONE_NAME)) {
            name == SingleSetterSubject.PROPERTY_ONE_NAME
            type == String
            writable
            !readable
        }
    }
}
