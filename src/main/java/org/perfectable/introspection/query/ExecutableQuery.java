package org.perfectable.introspection.query;

import java.lang.reflect.Executable;

import static java.util.Objects.requireNonNull;

public abstract class ExecutableQuery<E extends Executable, Q extends ExecutableQuery<E, ? extends Q>>
		extends MemberQuery<E, Q> {

	public abstract Q parameters(ParametersFilter parametersFilter);

	public Q parameters(Class<?>... parameterTypes) {
		requireNonNull(parameterTypes);
		return parameters(ParametersFilter.typesAccepted(parameterTypes));
	}

}
