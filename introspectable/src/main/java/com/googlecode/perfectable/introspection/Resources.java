package com.googlecode.perfectable.introspection;

import java.io.InputStream;

public final class Resources {

	public static InputStream load(String path) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	}

	private Resources() {
		// prevents initialization
	}
}
