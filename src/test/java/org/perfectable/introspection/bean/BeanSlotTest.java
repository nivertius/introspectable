package org.perfectable.introspection.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanSlotTest {
	@Test
	void test() {
		Subject instance = new Subject();
		BeanSlot<Subject> bean = BeanSlot.from(Subject.class);

		assertThat(bean)
			.returns(Subject.class, BeanSlot::type)
			.returns(Bean.from(instance), slot -> slot.put(instance));
	}

	static class Subject {
		// test class
	}

}
