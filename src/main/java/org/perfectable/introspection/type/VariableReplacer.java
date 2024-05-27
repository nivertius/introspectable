package org.perfectable.introspection.type;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
interface VariableReplacer {
	static VariableReplacer map(ImmutableMap<TypeVariable<? extends Class<?>>, Type> substitutions) {
		return new ForMap(substitutions);
	}

	static VariableReplacer view(TypeView view) {
		return new ForView(view);
	}

	Type replacementFor(Type type);

	final class ForView implements VariableReplacer {
		private final TypeView source;

		private final Map<Type, Type> mapping = new HashMap<>();

		ForView(TypeView source) {
			this.source = source;
		}

		@Override
		public Type replacementFor(Type type) {
			if (mapping.containsKey(type)) {
				return mapping.get(type);
			}
			Type replacement;
			if (type instanceof TypeVariable<?>) {
				TypeVariable<?> variable = (TypeVariable<?>) type;
				replacement = source.resolveVariable(variable);
			}
			else {
				replacement = TypeView.of(type).replaceVariables(this).unwrap();
			}
			mapping.put(type, replacement);
			return replacement;
		}
	}

	final class ForMap implements VariableReplacer {
		private final ImmutableMap<? extends TypeVariable<?>, Type> substitutions;

		ForMap(ImmutableMap<? extends TypeVariable<?>, Type> substitutions) {
			this.substitutions = substitutions;
		}

		@SuppressWarnings("nullness:cast.unsafe")
		@Override
		public Type replacementFor(Type type) {
			return (@NonNull Type) substitutions.getOrDefault(type, type);
		}
	}
}
