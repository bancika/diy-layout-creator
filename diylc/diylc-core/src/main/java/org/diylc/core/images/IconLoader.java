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
package org.diylc.core.images;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Loads image resources as Icons.
 *
 * @author Branislav Stojkovic
 */
public enum IconLoader {

  About("about.png"), Add("add.png"), BOM("bom.png"), Back("back.png"), BlackBoard(
      "blackboard.png"), Bug("bug.png"), CSV("csv.png"), Chest("chest.png"), Component(
      "component.png"), Copy("copy.png"), Cut("cut.png"), Delete("delete.png"), DiskBlue(
      "disk_blue.png"), DocumentEdit("document_edit.png"), DocumentPlainYellow(
      "document_plain_yellow.png"), Donate("donate.png"), EditComponent(
      "edit_component.png"), ElementsSelection("elements_selection.png"), Error("error.png"), Excel(
      "excel.png"), Exit("exit.png"), Faq("faq.png"), FolderOut("folder_out.png"), FormGreen(
      "form_green.png"), Front("front.png"), Garbage("garbage.png"), Gears("gears.png"), Group(
      "group.png"), HTML("html.png"), IconLarge("icon_large.png"), IconMedium(
      "icon_medium.png"), IconSmall("icon_small.png"), IdCard("id_card.png"), IdCardAdd(
      "id_card_add.png"), Image("image.png"), LightBulbOff("lightbulb_off.png"), LightBulbOn(
      "lightbulb_on.png"), Manual("manual.png"), MoveSmall("move_small.png"), NavigateCheck(
      "navigate_check.png"), NotebookAdd("notebook_add.png"), PDF("pdf.png"), Paste(
      "paste.png"), Pens("pens.png"), PhotoScenery("photo_scenery.png"), Plugin(
      "plugin.png"), Print("print.png"), SaveAs("save_as.png"), Schaller("schaller.png"), Selection(
      "selection.png"), Size("size.png"), Sort("sort.png"), TraceMask("trace_mask.png"), Undo(
      "undo.png"), Ungroup("ungroup.png"), Upload("upload.png"), Warning(
      "warning.png"), WindowColors("window_colors.png"), WindowGear("window_gear.png"), Wrench(
      "wrench.png"), ZoomSmall("zoom_small.png");

  protected String name;

  private IconLoader(String name) {
    this.name = name;
  }

  public Icon getIcon() {
    java.net.URL imgURL = getClass().getResource("/diylc-core-images/" + name);
    if (imgURL != null) {
      return new ImageIcon(imgURL, name);
    } else {
      Logger.getLogger(IconLoader.class).error("Couldn't find file: " + name);
      return null;
    }
  }

  public Image getImage() {
    BufferedImage img = null;
    try {
      img = ImageIO.read(getClass().getResourceAsStream("/diylc-core-images/" + name));
    } catch (IOException e) {
      Logger.getLogger(IconLoader.class).error("Couldn't find file: " + name);
    }
    return img;
  }
}
