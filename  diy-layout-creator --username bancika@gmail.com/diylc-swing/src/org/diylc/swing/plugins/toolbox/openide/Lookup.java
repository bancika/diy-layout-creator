//package org.diylc.swing.plugins.toolbox.openide;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//public abstract class Lookup {
//	public static final Lookup EMPTY = new Empty();
//	private static Lookup defaultLookup;
//
//	public static synchronized Lookup getDefault() {
//		if (defaultLookup != null) {
//			return defaultLookup;
//		}
//		String className = System.getProperty("org.openide.util.Lookup");
//		if ("-".equals(className)) {
//			return EMPTY;
//		}
//		ClassLoader l = Thread.currentThread().getContextClassLoader();
//		try {
//			if (className != null) {
//				defaultLookup = (Lookup) Class.forName(className, true, l)
//						.newInstance();
//
//				return defaultLookup;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		Lookup misl = Lookups.metaInfServices(l);
//		defaultLookup = (Lookup) misl.lookup(Lookup.class);
//		if (defaultLookup != null) {
//			return defaultLookup;
//		}
//		Provider prov = (Provider) misl.lookup(Provider.class);
//		if (prov != null) {
//			defaultLookup = Lookups.proxy(prov);
//
//			return defaultLookup;
//		}
//		DefLookup def = new DefLookup();
//		def.init(l, misl, false);
//		defaultLookup = def;
//		def.init(l, misl, true);
//		return defaultLookup;
//	}
//
//	private static final class DefLookup extends ProxyLookup {
//		public DefLookup() {
//			super();
//		}
//
//		public void init(ClassLoader loader, Lookup metaInfLookup,
//				boolean addPath) {
//			Lookup clLookup = Lookups.singleton(loader);
//			List<Lookup> arr = new ArrayList();
//			arr.add(metaInfLookup);
//			arr.add(clLookup);
//			String paths = System.getProperty("org.openide.util.Lookup.paths");
//			if ((addPath) && (paths != null)) {
//				for (String p : paths.split(":")) {
//					arr.add(Lookups.forPath(p));
//				}
//			}
//			setLookups((Lookup[]) arr.toArray(new Lookup[0]));
//		}
//	}
//
//	private static void resetDefaultLookup() {
//		if ((defaultLookup instanceof DefLookup)) {
//			DefLookup def = (DefLookup) defaultLookup;
//			ClassLoader l = Thread.currentThread().getContextClassLoader();
//			def.init(l, Lookups.metaInfServices(l), true);
//		}
//	}
//
//	public abstract <T> T lookup(Class<T> paramClass);
//
//	public abstract <T> Result<T> lookup(Template<T> paramTemplate);
//
//	public <T> Item<T> lookupItem(Template<T> template) {
//		Result<T> res = lookup(template);
//		Iterator<? extends Item<T>> it = res.allItems().iterator();
//		return it.hasNext() ? (Item) it.next() : null;
//	}
//
//	public <T> Result<T> lookupResult(Class<T> clazz) {
//		return lookup(new Template(clazz));
//	}
//
//	public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
//		return lookupResult(clazz).allInstances();
//	}
//
//	public static final class Template<T> {
//		private int hashCode;
//		private Class<T> type;
//		private String id;
//		private T instance;
//
//		@Deprecated
//		public Template() {
//			this(null);
//		}
//
//		public Template(Class<T> type) {
//			this(type, null, null);
//		}
//
//		public Template(Class<T> type, String id, T instance) {
//			this.type = extractType(type);
//			this.id = id;
//			this.instance = instance;
//		}
//
//		private Class<T> extractType(Class<T> type) {
//			return type == null ? Object.class : type;
//		}
//
//		public Class<T> getType() {
//			return this.type;
//		}
//
//		public String getId() {
//			return this.id;
//		}
//
//		public T getInstance() {
//			return (T) this.instance;
//		}
//
//		public int hashCode() {
//			if (this.hashCode != 0) {
//				return this.hashCode;
//			}
//			this.hashCode = ((this.type == null ? 1 : this.type.hashCode())
//					+ (this.id == null ? 2 : this.id.hashCode()) + (this.instance == null ? 3
//					: 0));
//
//			return this.hashCode;
//		}
//
//		public boolean equals(Object obj) {
//			if (!(obj instanceof Template)) {
//				return false;
//			}
//			Template t = (Template) obj;
//			if (hashCode() != t.hashCode()) {
//				return false;
//			}
//			if (this.type != t.type) {
//				return false;
//			}
//			if (this.id == null) {
//				if (t.id != null) {
//					return false;
//				}
//			} else if (!this.id.equals(t.id)) {
//				return false;
//			}
//			if (this.instance == null) {
//				return t.instance == null;
//			}
//			return this.instance.equals(t.instance);
//		}
//
//		public String toString() {
//			return "Lookup.Template[type=" + this.type + ",id=" + this.id
//					+ ",instance=" + this.instance + "]";
//		}
//	}
//
//	public static abstract class Result<T> {
//		public abstract void addLookupListener(
//				LookupListener paramLookupListener);
//
//		public abstract void removeLookupListener(
//				LookupListener paramLookupListener);
//
//		public abstract Collection<? extends T> allInstances();
//
//		public Set<Class<? extends T>> allClasses() {
//			return Collections.emptySet();
//		}
//
//		public Collection<? extends Lookup.Item<T>> allItems() {
//			return Collections.emptyList();
//		}
//	}
//
//	public static abstract class Item<T> {
//		public abstract T getInstance();
//
//		public abstract Class<? extends T> getType();
//
//		public abstract String getId();
//
//		public abstract String getDisplayName();
//
//		public String toString() {
//			return getId();
//		}
//	}
//
//	private static final class Empty extends Lookup {
//		private static final Lookup.Result NO_RESULT = new Lookup.Result() {
//			public void addLookupListener(LookupListener l) {
//			}
//
//			public void removeLookupListener(LookupListener l) {
//			}
//
//			public Collection allInstances() {
//				return Collections.EMPTY_SET;
//			}
//		};
//
//		public <T> T lookup(Class<T> clazz) {
//			return null;
//		}
//
//		public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
//			return NO_RESULT;
//		}
//	}
//
//	public static abstract interface Provider {
//		public abstract Lookup getLookup();
//	}
//}
