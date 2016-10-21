package org.diylc.swing.plugins.toolbox.openide;

public class Parameters {
	public static void notNull(CharSequence name, Object value) {
		if (value == null) {
			throw new NullPointerException("The " + name
					+ " parameter cannot be null");
		}
	}

	public static void notEmpty(CharSequence name, CharSequence value) {
		notNull(name, value);
		if (value.length() == 0) {
			throw new IllegalArgumentException("The " + name
					+ " parameter cannot be an empty character sequence");
		}
	}

	public static void notWhitespace(CharSequence name, CharSequence value) {
		notNull(name, value);
		if (value.toString().trim().length() == 0) {
			throw new IllegalArgumentException(
					"The "
							+ name
							+ " parameter must contain at least one non-whitespace character");
		}
	}

	public static void javaIdentifier(CharSequence name, CharSequence value) {
		notNull(name, value);
		javaIdentifierOrNull(name, value);
	}

	public static void javaIdentifierOrNull(CharSequence name,
			CharSequence value) {
		if ((value != null) && (!Utilities.isJavaIdentifier(value.toString()))) {
			throw new IllegalArgumentException("The " + name + " parameter ('"
					+ value + "') is not a valid Java identifier");
		}
	}
}
