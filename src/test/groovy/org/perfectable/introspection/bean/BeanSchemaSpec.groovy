package org.perfectable.introspection.bean

import spock.lang.Specification
import static org.perfectable.introspection.bean.BeanExamples.EmptySubject

class BeanSchemaSpec extends Specification {
    def "bean schema properly handles simple subject"() {
        given:
        def instance = new EmptySubject()
        
        when:
        def bean = BeanSchema.from(EmptySubject)
        
        then:
        bean.type() == EmptySubject
        bean.put(instance) == Bean.from(instance)
    }
}
