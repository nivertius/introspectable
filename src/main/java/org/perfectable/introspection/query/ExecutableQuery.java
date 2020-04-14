package org.perfectable.introspection.query;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

abstract class ExecutableQuery<E extends Executable, Q extends ExecutableQuery<E, ? extends Q>>
		extends MemberQuery<E, Q> {

	public abstract Q parameters(ParametersFilter parametersFilter);

	public Q parameters(Type... parameterTypes) {
		requireNonNull(parameterTypes);
		return parameters(ParametersFilter.typesAccepted(parameterTypes));
	}

	public Q parameterCount(int parameterCount) {
		return parameters(ParametersFilter.count(parameterCount));
	}
}
