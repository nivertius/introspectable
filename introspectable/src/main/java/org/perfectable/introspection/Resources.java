package org.perfectable.introspection;

import java.io.InputStream;

import javax.annotation.Nullable;

public final class Resources {
	
	@Nullable
	public static InputStream load(String path) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	}
	
	private Resources() {
		// prevents initialization
	}
}
