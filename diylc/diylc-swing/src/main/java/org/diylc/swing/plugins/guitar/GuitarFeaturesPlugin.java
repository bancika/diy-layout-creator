/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.plugins.guitar;

import java.util.EnumSet;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

/**
 * Adds the "Guitar features" toggle to the "View" menu - a single on/off switch (backed by
 * {@link #GUITAR_FEATURES_KEY}) for every guitar-pickup-specific piece of right-click UI this
 * class's sibling classes add ({@code ComponentPopupMenu}'s "Guitar Pickups" submenu, containing
 * "Select Pickup from Library..." and "Recolor Wires to Pickup Colors"). Reading this same key is
 * how {@code ComponentPopupMenu} decides, each time it is shown, whether to include that submenu
 * at all - so toggling it here takes effect immediately, with no restart needed.
 */
public class GuitarFeaturesPlugin implements IPlugIn {

  private static final String VIEW_TITLE = "View";

  /** Config key backing the "Guitar features" toggle; enabled by default. */
  public static final String GUITAR_FEATURES_KEY = "guitarFeaturesEnabled";

  private final ISwingUI swingUI;

  public GuitarFeaturesPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Guitar features", GUITAR_FEATURES_KEY, true),
        VIEW_TITLE);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    // no-op - this plugin only injects a static menu toggle.
  }
}
