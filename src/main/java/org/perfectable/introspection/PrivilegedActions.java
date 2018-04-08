package org.perfectable.introspection;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class PrivilegedActions {
	public static void markAccessible(AccessibleObject accessibleObject) {
		AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
			accessibleObject.setAccessible(true);
			return null;
		});
	}

	private PrivilegedActions() {
		// utility class
	}
}
