package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class StandardInvocationHandlerBuilderTest {
	
	private static final ProxyBuilder<TestEntity> PROXY_BUILDER = ProxyBuilderFactory.any()
			.ofInterfaces(TestEntity.class);
	
	@SuppressWarnings("boxing")
	@Test
	public void testBasic() {
		Marker<TestEntity> marker = Marker.empty();
		
		//@formatter:off
		InvocationHandler<TestEntity> handler =
				StandardInvocationHandlerBuilder.start(TestEntity.class)
						.bind(TestEntity::noArgumentNoResult)
							.to(marker::insert)
						.build(StandardObjectInvocationHandler.INSTANCE);
		//@formatter:on
		TestEntity proxy = PROXY_BUILDER.instantiate(handler);
		
		proxy.noArgumentNoResult();
		
		marker.verify(proxy);
	}
	
	public interface TestEntity {
		
		void noArgumentNoResult();
		
	}
	
	private static final class Marker<T> {
		@Nullable
		private T target;
		
		public static <T> Marker<T> empty() {
			return new Marker<>();
		}
		
		public void insert(T newTarget) {
			this.target = newTarget;
		}
		
		public void verify(T testTarget) {
			assertThat(this.target).isEqualTo(testTarget);
		}
		
	}
	
}
