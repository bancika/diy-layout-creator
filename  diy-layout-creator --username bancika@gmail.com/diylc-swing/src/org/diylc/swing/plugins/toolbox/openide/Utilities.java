package org.diylc.swing.plugins.toolbox.openide;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Utilities {
	private static final Logger LOG = Logger.getLogger(Utilities.class
			.getName());
	public static final int OS_WINNT = 1;
	public static final int OS_WIN95 = 2;
	public static final int OS_WIN98 = 4;
	public static final int OS_SOLARIS = 8;
	public static final int OS_LINUX = 16;
	public static final int OS_HP = 32;
	public static final int OS_AIX = 64;
	public static final int OS_IRIX = 128;
	public static final int OS_SUNOS = 256;
	public static final int OS_TRU64 = 512;
	@Deprecated
	public static final int OS_DEC = 1024;
	public static final int OS_OS2 = 2048;
	public static final int OS_MAC = 4096;
	public static final int OS_WIN2000 = 8192;
	public static final int OS_VMS = 16384;
	public static final int OS_WIN_OTHER = 32768;
	public static final int OS_OTHER = 65536;
	public static final int OS_FREEBSD = 131072;
	public static final int OS_WINVISTA = 262144;
	public static final int OS_UNIX_OTHER = 524288;
	public static final int OS_OPENBSD = 1048576;
	@Deprecated
	public static final int OS_WINDOWS_MASK = 303111;
	@Deprecated
	public static final int OS_UNIX_MASK = 1709048;
	public static final int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27;
	private static final int TYPICAL_MACOSX_MENU_HEIGHT = 24;
	private static int operatingSystem = -1;
	private static Timer clearIntrospector;
	private static ActionListener doClear;
	private static final int CTRL_WILDCARD_MASK = 32768;
	private static final int ALT_WILDCARD_MASK = 65536;
	private static final Object TRANS_LOCK = new Object();
	private static Object transLoader;
	private static RE transExp;
	private static Reference<NamesAndValues> namesAndValues;
	private static Method fileToPath;
	private static Method pathToUri;
	private static Method pathsGet;
	private static Method pathToFile;

	public static int getOperatingSystem() {
		if (operatingSystem == -1) {
			String osName = System.getProperty("os.name");
			if ("Windows NT".equals(osName)) {
				operatingSystem = 1;
			} else if ("Windows 95".equals(osName)) {
				operatingSystem = 2;
			} else if ("Windows 98".equals(osName)) {
				operatingSystem = 4;
			} else if ("Windows 2000".equals(osName)) {
				operatingSystem = 8192;
			} else if ("Windows Vista".equals(osName)) {
				operatingSystem = 262144;
			} else if (osName.startsWith("Windows ")) {
				operatingSystem = 32768;
			} else if ("Solaris".equals(osName)) {
				operatingSystem = 8;
			} else if (osName.startsWith("SunOS")) {
				operatingSystem = 8;
			} else if (osName.endsWith("Linux")) {
				operatingSystem = 16;
			} else if ("HP-UX".equals(osName)) {
				operatingSystem = 32;
			} else if ("AIX".equals(osName)) {
				operatingSystem = 64;
			} else if ("Irix".equals(osName)) {
				operatingSystem = 128;
			} else if ("SunOS".equals(osName)) {
				operatingSystem = 256;
			} else if ("Digital UNIX".equals(osName)) {
				operatingSystem = 512;
			} else if ("OS/2".equals(osName)) {
				operatingSystem = 2048;
			} else if ("OpenVMS".equals(osName)) {
				operatingSystem = 16384;
			} else if (osName.equals("Mac OS X")) {
				operatingSystem = 4096;
			} else if (osName.startsWith("Darwin")) {
				operatingSystem = 4096;
			} else if (osName.toLowerCase(Locale.US).startsWith("freebsd")) {
				operatingSystem = 131072;
			} else if ("OpenBSD".equals(osName)) {
				operatingSystem = 1048576;
			} else if (File.pathSeparatorChar == ':') {
				operatingSystem = 524288;
			} else {
				operatingSystem = 65536;
			}
		}
		return operatingSystem;
	}

	public static boolean isWindows() {
		return (getOperatingSystem() & 0x4A007) != 0;
	}

	public static boolean isMac() {
		return (getOperatingSystem() & 0x1000) != 0;
	}

	public static boolean isUnix() {
		return (getOperatingSystem() & 0x1A13F8) != 0;
	}

	static void resetOperatingSystem() {
		operatingSystem = -1;
	}

	public static boolean isJavaIdentifier(String id) {
		if (id == null) {
			return false;
		}
		return (SourceVersion.isIdentifier(id))
				&& (!SourceVersion.isKeyword(id));
	}

	public static BeanInfo getBeanInfo(Class<?> clazz)
			throws IntrospectionException {
		BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException ie) {
			throw ie;
		} catch (Error e) {
			throw e;
		}
		if (Component.class.isAssignableFrom(clazz)) {
			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				if (pds[i].getName().equals("cursor")) {
					try {
						Method getter = Component.class.getDeclaredMethod(
								"getCursor", new Class[0]);
						Method setter = Component.class.getDeclaredMethod(
								"setCursor", new Class[] { Cursor.class });
						pds[i] = new PropertyDescriptor("cursor", getter,
								setter);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (bi != null) {
			if (clearIntrospector == null) {
				doClear = new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
					}
				};
				clearIntrospector = new Timer(15000, doClear);
				clearIntrospector.setRepeats(false);
			}
			clearIntrospector.restart();
		}
		return bi;
	}

	public static BeanInfo getBeanInfo(Class<?> clazz, Class<?> stopClass)
			throws IntrospectionException {
		return Introspector.getBeanInfo(clazz, stopClass);
	}

	private static String trimString(String s) {
		int idx = 0;

		int slen = s.length();
		if (slen == 0) {
			return s;
		}
		char c;
		do {
			c = s.charAt(idx++);
		} while (((c == '\n') || (c == '\r')) && (idx < slen));
		s = s.substring(--idx);
		idx = s.length() - 1;
		if (idx < 0) {
			return s;
		}
		do {
			c = s.charAt(idx--);
		} while (((c == '\n') || (c == '\r')) && (idx >= 0));
		return s.substring(0, idx + 2);
	}

	public static String pureClassName(String fullName) {
		int index = fullName.indexOf('$');
		if ((index >= 0) && (index < fullName.length())) {
			return fullName.substring(index + 1, fullName.length());
		}
		return fullName;
	}

	@Deprecated
	public static boolean isLargeFrameIcons() {
		return (getOperatingSystem() == 8) || (getOperatingSystem() == 32);
	}

	@Deprecated
	public static int arrayHashCode(Object[] arr) {
		int c = 0;
		int len = arr.length;
		for (int i = 0; i < len; i++) {
			Object o = arr[i];
			int v = o == null ? 1 : o.hashCode();
			c += (v ^ i);
		}
		return c;
	}

	public static boolean compareObjects(Object o1, Object o2) {
		return compareObjectsImpl(o1, o2, 1);
	}

	public static boolean compareObjectsImpl(Object o1, Object o2,
			int checkArraysDepth) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o2 == null) {
			return false;
		}
		if (checkArraysDepth > 0) {
			if (((o1 instanceof Object[])) && ((o2 instanceof Object[]))) {
				Object[] o1a = (Object[]) o1;
				Object[] o2a = (Object[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (!compareObjectsImpl(o1a[i], o2a[i],
							checkArraysDepth - 1)) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof byte[])) && ((o2 instanceof byte[]))) {
				byte[] o1a = (byte[]) o1;
				byte[] o2a = (byte[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof short[])) && ((o2 instanceof short[]))) {
				short[] o1a = (short[]) o1;
				short[] o2a = (short[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof int[])) && ((o2 instanceof int[]))) {
				int[] o1a = (int[]) o1;
				int[] o2a = (int[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof long[])) && ((o2 instanceof long[]))) {
				long[] o1a = (long[]) o1;
				long[] o2a = (long[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof float[])) && ((o2 instanceof float[]))) {
				float[] o1a = (float[]) o1;
				float[] o2a = (float[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof double[])) && ((o2 instanceof double[]))) {
				double[] o1a = (double[]) o1;
				double[] o2a = (double[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof char[])) && ((o2 instanceof char[]))) {
				char[] o1a = (char[]) o1;
				char[] o2a = (char[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
			if (((o1 instanceof boolean[])) && ((o2 instanceof boolean[]))) {
				boolean[] o1a = (boolean[]) o1;
				boolean[] o2a = (boolean[]) o2;
				int l1 = o1a.length;
				int l2 = o2a.length;
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					if (o1a[i] != o2a[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return o1.equals(o2);
	}

	public static String getClassName(Class<?> clazz) {
		if (clazz.isArray()) {
			return getClassName(clazz.getComponentType()) + "[]";
		}
		return clazz.getName();
	}

	public static String getShortClassName(Class<?> clazz) {
		if (clazz.isArray()) {
			return getShortClassName(clazz.getComponentType()) + "[]";
		}
		String name = clazz.getName().replace('$', '.');

		return name.substring(name.lastIndexOf('.') + 1, name.length());
	}

	public static Object[] toObjectArray(Object array) {
		if ((array instanceof Object[])) {
			return (Object[]) array;
		}
		if ((array instanceof int[])) {
			int k = ((int[]) array).length;
			Integer[] r = new Integer[k];
			for (int i = 0; i < k; i++) {
				r[i] = Integer.valueOf(((int[]) (int[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof boolean[])) {
			int k = ((boolean[]) array).length;
			Boolean[] r = new Boolean[k];
			for (int i = 0; i < k; i++) {
				r[i] = Boolean.valueOf(((boolean[]) (boolean[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof byte[])) {
			int k = ((byte[]) array).length;
			Byte[] r = new Byte[k];
			for (int i = 0; i < k; i++) {
				r[i] = Byte.valueOf(((byte[]) (byte[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof char[])) {
			int k = ((char[]) array).length;
			Character[] r = new Character[k];
			for (int i = 0; i < k; i++) {
				r[i] = Character.valueOf(((char[]) (char[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof double[])) {
			int k = ((double[]) array).length;
			Double[] r = new Double[k];
			for (int i = 0; i < k; i++) {
				r[i] = Double.valueOf(((double[]) (double[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof float[])) {
			int k = ((float[]) array).length;
			Float[] r = new Float[k];
			for (int i = 0; i < k; i++) {
				r[i] = Float.valueOf(((float[]) (float[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof long[])) {
			int k = ((long[]) array).length;
			Long[] r = new Long[k];
			for (int i = 0; i < k; i++) {
				r[i] = Long.valueOf(((long[]) (long[]) array)[i]);
			}
			return r;
		}
		if ((array instanceof short[])) {
			int k = ((short[]) array).length;
			Short[] r = new Short[k];
			for (int i = 0; i < k; i++) {
				r[i] = Short.valueOf(((short[]) (short[]) array)[i]);
			}
			return r;
		}
		throw new IllegalArgumentException();
	}

	public static Class<?> getObjectType(Class<?> c) {
		if (!c.isPrimitive()) {
			return c;
		}
		if (c == Integer.TYPE) {
			return Integer.class;
		}
		if (c == Boolean.TYPE) {
			return Boolean.class;
		}
		if (c == Byte.TYPE) {
			return Byte.class;
		}
		if (c == Character.TYPE) {
			return Character.class;
		}
		if (c == Double.TYPE) {
			return Double.class;
		}
		if (c == Float.TYPE) {
			return Float.class;
		}
		if (c == Long.TYPE) {
			return Long.class;
		}
		if (c == Short.TYPE) {
			return Short.class;
		}
		throw new IllegalArgumentException();
	}

	public static Class<?> getPrimitiveType(Class<?> c) {
		if (!c.isPrimitive()) {
			return c;
		}
		if (c == Integer.class) {
			return Integer.TYPE;
		}
		if (c == Boolean.class) {
			return Boolean.TYPE;
		}
		if (c == Byte.class) {
			return Byte.TYPE;
		}
		if (c == Character.class) {
			return Character.TYPE;
		}
		if (c == Double.class) {
			return Double.TYPE;
		}
		if (c == Float.class) {
			return Float.TYPE;
		}
		if (c == Long.class) {
			return Long.TYPE;
		}
		if (c == Short.class) {
			return Short.TYPE;
		}
		throw new IllegalArgumentException();
	}

	public static Component getFocusTraversableComponent(Component c) {
		if (c.isFocusable()) {
			return c;
		}
		if (!(c instanceof Container)) {
			return null;
		}
		int k = ((Container) c).getComponentCount();
		for (int i = 0; i < k; i++) {
			Component v = ((Container) c).getComponent(i);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	public static String[] parseParameters(String s) {
		int NULL = 0;
		int INPARAM = 1;
		int INPARAMPENDING = 2;
		int STICK = 4;
		int STICKPENDING = 8;
		Vector<String> params = new Vector(5, 5);

		int state = NULL;
		StringBuilder buff = new StringBuilder(20);
		int slength = s.length();
		for (int i = 0; i < slength; i++) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				if (state == NULL) {
					if (buff.length() > 0) {
						params.addElement(buff.toString());
						buff.setLength(0);
					}
				} else if (state == STICK) {
					params.addElement(buff.toString());
					buff.setLength(0);
					state = NULL;
				} else if (state == STICKPENDING) {
					buff.append('\\');
					params.addElement(buff.toString());
					buff.setLength(0);
					state = NULL;
				} else if (state == INPARAMPENDING) {
					state = INPARAM;
					buff.append('\\');
					buff.append(c);
				} else {
					buff.append(c);
				}
			} else if (c == '\\') {
				if (state == NULL) {
					i++;
					if (i < slength) {
						char cc = s.charAt(i);
						if ((cc == '"') || (cc == '\\')) {
							buff.append(cc);
						} else if (Character.isWhitespace(cc)) {
							buff.append(c);
							i--;
						} else {
							buff.append(c);
							buff.append(cc);
						}
					} else {
						buff.append('\\');

						break;
					}
				} else if (state == INPARAM) {
					state = INPARAMPENDING;
				} else if (state == INPARAMPENDING) {
					buff.append('\\');
					state = INPARAM;
				} else if (state == STICK) {
					state = STICKPENDING;
				} else if (state == STICKPENDING) {
					buff.append('\\');
					state = STICK;
				}
			} else if (c == '"') {
				if (state == NULL) {
					state = INPARAM;
				} else if (state == INPARAM) {
					state = STICK;
				} else if (state == STICK) {
					state = INPARAM;
				} else if (state == STICKPENDING) {
					buff.append('"');
					state = STICK;
				} else {
					buff.append('"');
					state = INPARAM;
				}
			} else {
				if (state == INPARAMPENDING) {
					buff.append('\\');
					state = INPARAM;
				} else if (state == STICKPENDING) {
					buff.append('\\');
					state = STICK;
				}
				buff.append(c);
			}
		}
		if (state == INPARAM) {
			params.addElement(buff.toString());
		} else if ((state & (INPARAMPENDING | STICKPENDING)) != 0) {
			buff.append('\\');
			params.addElement(buff.toString());
		} else if (buff.length() != 0) {
			params.addElement(buff.toString());
		}
		String[] ret = new String[params.size()];
		params.copyInto(ret);

		return ret;
	}

	public static String escapeParameters(String[] params) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			escapeString(params[i], sb);
			sb.append(' ');
		}
		int len = sb.length();
		if (len > 0) {
			sb.setLength(len - 1);
		}
		return sb.toString().trim();
	}

	private static void escapeString(String s, StringBuffer sb) {
		if (s.length() == 0) {
			sb.append("\"\"");

			return;
		}
		boolean hasSpace = false;
		int sz = sb.length();
		int slen = s.length();
		for (int i = 0; i < slen; i++) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				hasSpace = true;
				sb.append(c);
			} else if (c == '\\') {
				sb.append('\\').append('\\');
			} else if (c == '"') {
				sb.append('\\').append('"');
			} else {
				sb.append(c);
			}
		}
		if (hasSpace) {
			sb.insert(sz, '"');
			sb.append('"');
		}
	}

	private static final class NamesAndValues {
		final Map<Integer, String> keyToString;
		final Map<String, Integer> stringToKey;

		NamesAndValues(Map<Integer, String> keyToString,
				Map<String, Integer> stringToKey) {
			this.keyToString = keyToString;
			this.stringToKey = stringToKey;
		}
	}

	private static synchronized NamesAndValues initNameAndValues() {
		if (namesAndValues != null) {
			NamesAndValues nav = (NamesAndValues) namesAndValues.get();
			if (nav != null) {
				return nav;
			}
		}
		Field[] fields = KeyEvent.class.getDeclaredFields();

		Map<String, Integer> names = new HashMap(fields.length * 4 / 3 + 5,
				0.75F);
		Map<Integer, String> values = new HashMap(fields.length * 4 / 3 + 5,
				0.75F);
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) {
				String name = f.getName();
				if (name.startsWith("VK_")) {
					name = name.substring(3);
					try {
						int numb = f.getInt(null);
						names.put(name, Integer.valueOf(numb));
						values.put(Integer.valueOf(numb), name);
					} catch (IllegalArgumentException ex) {
					} catch (IllegalAccessException ex) {
					}
				}
			}
		}
		if (names.get("CONTEXT_MENU") == null) {
			names.put("CONTEXT_MENU", Integer.valueOf(524));
			values.put(Integer.valueOf(524), "CONTEXT_MENU");
			names.put("WINDOWS", Integer.valueOf(525));
			values.put(Integer.valueOf(525), "WINDOWS");
		}
		names.put("MOUSE_WHEEL_UP", Integer.valueOf(656));
		names.put("MOUSE_WHEEL_DOWN", Integer.valueOf(657));
		values.put(Integer.valueOf(656), "MOUSE_WHEEL_UP");
		values.put(Integer.valueOf(657), "MOUSE_WHEEL_DOWN");

		NamesAndValues nav = new NamesAndValues(values, names);
		namesAndValues = new SoftReference(nav);
		return nav;
	}

	public static String keyToString(KeyStroke stroke) {
		StringBuilder sb = new StringBuilder();
		if (addModifiers(sb, stroke.getModifiers())) {
			sb.append('-');
		}
		appendRest(sb, stroke);
		return sb.toString();
	}

	private static void appendRest(StringBuilder sb, KeyStroke stroke) {
		String c = (String) initNameAndValues().keyToString.get(Integer
				.valueOf(stroke.getKeyCode()));
		if (c == null) {
			sb.append(stroke.getKeyChar());
		} else {
			sb.append(c);
		}
	}

	public static String keyToString(KeyStroke stroke, boolean portable) {
		if (portable) {
			StringBuilder sb = new StringBuilder();
			if (addModifiersPortable(sb, stroke.getModifiers())) {
				sb.append('-');
			}
			appendRest(sb, stroke);
			return sb.toString();
		}
		return keyToString(stroke);
	}

	private static boolean usableKeyOnMac(int key, int mask) {
		if (key == 81) {
			return false;
		}
		boolean isMeta = ((mask & 0x4) != 0) || ((mask & 0x80) != 0);

		boolean isAlt = ((mask & 0x8) != 0) || ((mask & 0x200) != 0);

		boolean isOnlyMeta = (isMeta) && ((mask & 0xFEFB) == 0);
		if (isOnlyMeta) {
			return (key != 72) && (key != 32) && (key != 9);
		}
		if ((key == 68) && (isMeta) && (isAlt)) {
			return false;
		}
		if ((key == 32) && (isMeta) && ((mask & 0x2) != 0)) {
			return false;
		}
		return true;
	}

	private static int getMenuShortcutKeyMask() {
		try {
			if (!GraphicsEnvironment.isHeadless()) {
				return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			}
		} catch (Throwable ex) {
		}
		return 2;
	}

	private static boolean addModifiers(StringBuilder buf, int modif) {
		boolean b = false;
		if ((modif & 0x2) != 0) {
			buf.append("C");
			b = true;
		}
		if ((modif & 0x8) != 0) {
			buf.append("A");
			b = true;
		}
		if ((modif & 0x1) != 0) {
			buf.append("S");
			b = true;
		}
		if ((modif & 0x4) != 0) {
			buf.append("M");
			b = true;
		}
		if ((modif & 0x8000) != 0) {
			buf.append("D");
			b = true;
		}
		if ((modif & 0x10000) != 0) {
			buf.append("O");
			b = true;
		}
		return b;
	}

	private static boolean addModifiersPortable(StringBuilder buf, int modifiers) {
		boolean b = false;
		if ((modifiers & 0x1) != 0) {
			buf.append('S');
			b = true;
		}
		if (((isMac()) && ((modifiers & 0x4) != 0))
				|| ((!isMac()) && ((modifiers & 0x2) != 0))) {
			buf.append('D');
			b = true;
		}
		if (((isMac()) && ((modifiers & 0x2) != 0))
				|| ((!isMac()) && ((modifiers & 0x8) != 0))) {
			buf.append('O');
			b = true;
		}
		if ((isMac()) && ((modifiers & 0x8) != 0)) {
			buf.append('A');
			b = true;
		}
		return b;
	}

	private static int readModifiers(String s) throws NoSuchElementException {
		int m = 0;
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case 'C':
				m |= 0x2;

				break;
			case 'A':
				m |= 0x8;

				break;
			case 'M':
				m |= 0x4;

				break;
			case 'S':
				m |= 0x1;

				break;
			case 'D':
				m |= 0x8000;

				break;
			case 'O':
				m |= 0x10000;

				break;
			case 'B':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'N':
			case 'P':
			case 'Q':
			case 'R':
			default:
				throw new NoSuchElementException(s);
			}
		}
		return m;
	}

	private static GraphicsConfiguration getCurrentGraphicsConfiguration() {
		Component focusOwner = KeyboardFocusManager
				.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner != null) {
			Window w = SwingUtilities.getWindowAncestor(focusOwner);
			if (w != null) {
				return w.getGraphicsConfiguration();
			}
		}
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
	}

	public static Rectangle getUsableScreenBounds() {
		return getUsableScreenBounds(getCurrentGraphicsConfiguration());
	}

	public static Rectangle getUsableScreenBounds(GraphicsConfiguration gconf) {
		if (gconf == null) {
			gconf = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice().getDefaultConfiguration();
		}
		Rectangle bounds = new Rectangle(gconf.getBounds());

		String str = System.getProperty("netbeans.screen.insets");
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, ", ");
			if (st.countTokens() == 4) {
				try {
					bounds.y = Integer.parseInt(st.nextToken());
					bounds.x = Integer.parseInt(st.nextToken());
					bounds.height -= bounds.y
							+ Integer.parseInt(st.nextToken());
					bounds.width -= bounds.x + Integer.parseInt(st.nextToken());
				} catch (NumberFormatException ex) {
					LOG.log(Level.WARNING, null, ex);
				}
			}
			return bounds;
		}
		str = System.getProperty("netbeans.taskbar.height");
		if (str != null) {
			bounds.height -= Integer.getInteger(str, 0).intValue();

			return bounds;
		}
		try {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Insets insets = toolkit.getScreenInsets(gconf);
			bounds.y += insets.top;
			bounds.x += insets.left;
			bounds.height -= insets.top + insets.bottom;
			bounds.width -= insets.left + insets.right;
		} catch (Exception ex) {
			LOG.log(Level.WARNING, null, ex);
		}
		return bounds;
	}

	public static Rectangle findCenterBounds(Dimension componentSize) {
		return findCenterBounds(getCurrentGraphicsConfiguration(),
				componentSize);
	}

	private static Rectangle findCenterBounds(GraphicsConfiguration gconf,
			Dimension componentSize) {
		if (gconf == null) {
			gconf = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice().getDefaultConfiguration();
		}
		Rectangle bounds = gconf.getBounds();

		return new Rectangle(bounds.x + (bounds.width - componentSize.width)
				/ 2, bounds.y + (bounds.height - componentSize.height) / 2,
				componentSize.width, componentSize.height);
	}

	private static void loadTranslationFile(RE re, BufferedReader reader,
			Set<String[]> results) throws IOException {
		for (;;) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			if ((line.length() != 0) && (!line.startsWith("#"))) {
				String[] pair = re.readPair(line);
				if (pair == null) {
					throw new InvalidObjectException("Line is invalid: " + line);
				}
				results.add(pair);
			}
		}
	}

	@Deprecated
	public static Image icon2Image(Icon icon) {
		return ImageUtilities.icon2Image(icon);
	}	

	static {
		try {
			fileToPath = File.class.getMethod("toPath", new Class[0]);
		} catch (NoSuchMethodException x) {
		}
		if (fileToPath != null) {
			try {
				Class<?> path = Class.forName("java.nio.file.Path");
				pathToUri = path.getMethod("toUri", new Class[0]);
				pathsGet = Class.forName("java.nio.file.Paths").getMethod(
						"get", new Class[] { URI.class });
				pathToFile = path.getMethod("toFile", new Class[0]);
			} catch (Exception x) {
				throw new ExceptionInInitializerError(x);
			}
		}
	}

	public static URI toURI(File f) {
		if (fileToPath != null) {
			try {
				URI u = (URI) pathToUri.invoke(
						fileToPath.invoke(f, new Object[0]), new Object[0]);
				if (u.toString().startsWith("file:///")) {
				}
				return new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
						u.getPort(), u.getPath(), u.getQuery(), u.getFragment());
			} catch (Exception x) {
				LOG.log(Level.FINE, "could not convert " + f + " to URI", x);
			}
		}
		String path = f.getAbsolutePath();
		if (path.startsWith("\\\\")) {
			if ((!path.endsWith("\\")) && (f.isDirectory())) {
				path = path + "\\";
			}
			try {
				return new URI("file", null, path.replace('\\', '/'), null);
			} catch (URISyntaxException x) {
				LOG.log(Level.FINE, "could not convert " + f + " to URI", x);
			}
		}
		return f.toURI();
	}

	public static File toFile(URI u) throws IllegalArgumentException {
		if (pathsGet != null) {
			try {
				return (File) pathToFile.invoke(
						pathsGet.invoke(null, new Object[] { u }),
						new Object[0]);
			} catch (Exception x) {
				LOG.log(Level.FINE, "could not convert " + u + " to File", x);
			}
		}
		String host = u.getHost();
		if ((host != null) && (!host.isEmpty())
				&& ("file".equals(u.getScheme()))) {
			return new File("\\\\" + host + u.getPath().replace('/', '\\'));
		}
		return new File(u);
	}

	@Deprecated
	public static URL toURL(File f) throws MalformedURLException {
		if (f == null) {
			throw new NullPointerException();
		}
		if (!f.isAbsolute()) {
			throw new IllegalArgumentException("Relative path: " + f);
		}
		URI uri = toURI(f);

		return uri.toURL();
	}

	@Deprecated
	public static File toFile(URL u) {
		if (u == null) {
			throw new NullPointerException();
		}
		try {
			URI uri = u.toURI();

			return toFile(uri);
		} catch (URISyntaxException use) {
			return null;
		} catch (IllegalArgumentException iae) {
		}
		return null;
	}

	static abstract interface RE {
		public abstract void init(String[] paramArrayOfString1,
				String[] paramArrayOfString2);

		public abstract String convert(String paramString);

		public abstract String[] readPair(String paramString);
	}

	@Deprecated
	public static class UnorderableException extends RuntimeException {
		static final long serialVersionUID = 6749951134051806661L;
		private Collection unorderable;
		private Map deps;

		public UnorderableException(Collection unorderable, Map deps) {
			this.unorderable = unorderable;
			this.deps = deps;
		}

		public UnorderableException(String message, Collection unorderable,
				Map deps) {
			super();
			this.unorderable = unorderable;
			this.deps = deps;
		}

		public Collection getUnorderable() {
			return this.unorderable;
		}

		public Map getDeps() {
			return this.deps;
		}
	}
}
