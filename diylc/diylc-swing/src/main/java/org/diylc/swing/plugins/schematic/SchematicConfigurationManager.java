/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.schematic;

import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.IPlugInPort;

/**
 * The application's configuration as the schematic canvas sees it: everything is delegated, except
 * that locked components are never dimmed.
 * <p>
 * The schematic keeps its whole wiring layer locked so that generated wires cannot be selected,
 * dragged or deleted. Locked components are normally painted at half alpha to signal that they are
 * untouchable, which here would wash out every wire on the drawing and read as a rendering fault
 * rather than a signal. Overriding the preference for this canvas alone leaves the user's own
 * setting untouched on the layout canvas.
 *
 * @author Branislav Stojkovic
 */
public class SchematicConfigurationManager<T> implements IConfigurationManager<T> {

  private final IConfigurationManager<T> delegate;

  public static <T> IConfigurationManager<T> wrap(IConfigurationManager<T> delegate) {
    return new SchematicConfigurationManager<T>(delegate);
  }

  private SchematicConfigurationManager(IConfigurationManager<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean readBoolean(String key, boolean defaultValue) {
    if (IPlugInPort.LOCKED_ALPHA.equals(key)) {
      return false;
    }
    return delegate.readBoolean(key, defaultValue);
  }

  @Override
  public void addConfigListener(String key, IConfigListener listener) {
    delegate.addConfigListener(key, listener);
  }

  @Override
  public String readString(String key, String defaultValue) {
    return delegate.readString(key, defaultValue);
  }

  @Override
  public int readInt(String key, int defaultValue) {
    return delegate.readInt(key, defaultValue);
  }

  @Override
  public float readFloat(String key, float defaultValue) {
    return delegate.readFloat(key, defaultValue);
  }

  @Override
  public double readDouble(String key, double defaultValue) {
    return delegate.readDouble(key, defaultValue);
  }

  @Override
  public Object readObject(String key, Object defaultValue) {
    return delegate.readObject(key, defaultValue);
  }

  @Override
  public void writeValue(String key, Object value) {
    delegate.writeValue(key, value);
  }

  @Override
  public T getSerializer() {
    return delegate.getSerializer();
  }

  @Override
  public void initialize(String appName) {
    delegate.initialize(appName);
  }
}
