/*
 *
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.components.displays;

import java.awt.Color;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.components.displays.CharacterLCD.LCDInterface;
import org.diylc.components.displays.CharacterLCD.LCDSize;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * What the character LCD prints on its screen. The module's two sizes and two interfaces are the
 * whole of what a buyer chooses between, and neither is legible from a green board with a blue
 * window, so the lit area carries them.
 */
public class CharacterLCDTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  private static CharacterLCD of(LCDSize lcdSize, LCDInterface lcdInterface) {
    CharacterLCD display = new CharacterLCD();
    display.setLcdSize(lcdSize);
    display.setLcdInterface(lcdInterface);
    return display;
  }

  @Test
  public void everyScreenPrintsItsSizeAndInterface() {
    for (LCDSize lcdSize : LCDSize.values()) {
      for (LCDInterface lcdInterface : LCDInterface.values()) {
        CharacterLCD printed = of(lcdSize, lcdInterface);
        CharacterLCD blank = of(lcdSize, lcdInterface);
        blank.setScreen(Display.NONE);

        Assert.assertEquals(Display.VALUE, printed.getScreen());
        Assert.assertEquals(lcdSize + ", " + lcdInterface, printed.getValueForDisplay());
        MakerBoardTestSupport.assertScreenTextIsDrawn(printed, blank);
      }
    }
  }

  /**
   * Both inks exist on real modules -- the blue-backlit one shows light characters and the
   * yellow-green one dark -- so the text has to survive a screen colour the user picks. Two
   * backlights at opposite ends of the range must not render the same, or one of them is printing
   * its text in the backlight's own colour.
   */
  @Test
  public void theInkFollowsTheBacklight() {
    CharacterLCD dark = new CharacterLCD();
    dark.setScreenColor(Color.decode("#1E88E5"));
    CharacterLCD pale = new CharacterLCD();
    pale.setScreenColor(Color.decode("#CDDC39"));

    CharacterLCD darkBlank = new CharacterLCD();
    darkBlank.setScreenColor(Color.decode("#1E88E5"));
    darkBlank.setScreen(Display.NONE);
    CharacterLCD paleBlank = new CharacterLCD();
    paleBlank.setScreenColor(Color.decode("#CDDC39"));
    paleBlank.setScreen(Display.NONE);

    MakerBoardTestSupport.assertScreenTextIsDrawn(dark, darkBlank);
    MakerBoardTestSupport.assertScreenTextIsDrawn(pale, paleBlank);
  }

  @Test
  public void drawsCleanlyInEveryState() {
    for (LCDSize lcdSize : LCDSize.values()) {
      for (LCDInterface lcdInterface : LCDInterface.values()) {
        for (Display screen : Display.values()) {
          CharacterLCD display = of(lcdSize, lcdInterface);
          display.setScreen(screen);
          MakerBoardTestSupport.assertDrawsCleanly(display);
        }
      }
    }
  }
}
