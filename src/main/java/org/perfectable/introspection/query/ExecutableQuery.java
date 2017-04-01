package org.perfectable.introspection.query;

import java.lang.reflect.Executable;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ExecutableQuery<E extends Executable, Q extends ExecutableQuery<E, ? extends Q>>
		extends MemberQuery<E, Q> {

	public abstract Q parameters(ParametersFilter parametersFilter);

	public Q parameters(Class<?>... parameterTypes) {
		checkNotNull(parameterTypes);
		return parameters(ParametersFilter.types(parameterTypes));
	}

}
