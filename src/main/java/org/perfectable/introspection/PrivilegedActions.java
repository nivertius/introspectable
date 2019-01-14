package org.perfectable.introspection;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Helper class that allows executing code that requires {@link AccessController#doPrivileged}.
 */
public final class PrivilegedActions {
	/**
	 * Marks object as accessible with {@link AccessibleObject#setAccessible}.
	 *
	 * @param accessibleObject object to be marked as accessible
	 */
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
