package org.diylc.swing.plugins.toolbox.openide;

import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

public class NbBundle {

	private static String brandingToken = null;

	public static String getBranding() {
		return brandingToken;
	}

	public static Iterator<String> getLocalizingSuffixes() {
		return new LocaleIterator(Locale.getDefault());
	}

	private static class LocaleIterator implements Iterator<String> {
		private boolean defaultInProgress = false;
		private boolean empty = false;
		private Locale locale;
		private Locale initLocale;
		private String current;
		private String branding;

		public LocaleIterator(Locale locale) {
			this.locale = (this.initLocale = locale);
			if (locale.equals(Locale.getDefault())) {
				this.defaultInProgress = true;
			}
			this.current = ('_' + locale.toString());
			if (NbBundle.brandingToken == null) {
				this.branding = null;
			} else {
				this.branding = ("_" + NbBundle.brandingToken);
			}
		}

		public String next() throws NoSuchElementException {
			if (this.current == null) {
				throw new NoSuchElementException();
			}
			String ret;
			if (this.branding == null) {
				ret = this.current;
			} else {
				ret = this.branding + this.current;
			}
			int lastUnderbar = this.current.lastIndexOf('_');
			if (lastUnderbar == 0) {
				if (this.empty) {
					reset();
				} else {
					this.current = "";
					this.empty = true;
				}
			} else if (lastUnderbar == -1) {
				if (this.defaultInProgress) {
					reset();
				} else {
					this.locale = Locale.getDefault();
					this.current = ('_' + this.locale.toString());
					this.defaultInProgress = true;
				}
			} else {
				this.current = this.current.substring(0, lastUnderbar);
			}
			return ret;
		}

		private void reset() {
			if (this.branding != null) {
				this.current = ('_' + this.initLocale.toString());

				int idx = this.branding.lastIndexOf('_');
				if (idx == 0) {
					this.branding = null;
				} else {
					this.branding = this.branding.substring(0, idx);
				}
				this.empty = false;
			} else {
				this.current = null;
			}
		}

		public boolean hasNext() {
			return this.current != null;
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
