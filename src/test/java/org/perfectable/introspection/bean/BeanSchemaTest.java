package org.perfectable.introspection.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanSchemaTest {
	@Test
	void test() {
		Subject instance = new Subject();
		BeanSchema<Subject> bean = BeanSchema.from(Subject.class);

		assertThat(bean)
			.returns(Subject.class, BeanSchema::type)
			.returns(Bean.from(instance), slot -> slot.put(instance));
	}

	static class Subject {
		// test class
	}

}
