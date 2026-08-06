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
package org.diylc.utils;

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

  About("about.png"), Add("add.png"), ApplicationEdit("application_edit.png"), Arrow(
      "arrow.png"), BOM("bom.png"), Back("back.png"), Barcode("barcode.png"), BlackBoard(
      "blackboard.png"), Brain("brain.png"), BrainBig("brain_big.png"), BranchAdd(
      "branch_add.png"), BriefcaseAdd("briefcase_add.png"), BriefcaseInto(
      "briefcase_into.png"), Bug("bug.png"), CSV("csv.png"), Chest("chest.png"), Cloud(
      "cloud.png"), CloudBg("cloud_bg.png"), CloudBig("cloud_big.png"), CloudDelete(
      "cloud_delete.png"), CloudDownload("cloud_download.png"), CloudEdit(
      "cloud_edit.png"), CloudGear("cloud_gear.png"), CloudUp("cloud_up.png"), CloudUpload(
      "cloud_upload.png"), CloudWait("cloud_wait.png"), CoffeebeanEdit(
      "coffeebean_edit.png"), Component("component.png"), ComponentAdd(
      "component_add.png"), ComponentLarge("component_large.png"), ComponentPreferences(
      "component_preferences.png"), ComponentReplace("component_replace.png"), Copy(
      "copy.png"), Cut("cut.png"), Dashboard("dashboard.png"), DataFind("data_find.png"), Delete(
      "delete.png"), DiskBlue("disk_blue.png"), DocumentEdit("document_edit.png"), DocumentGerber(
      "document_gerber.png"), DocumentInfo("document_info.png"), DocumentPlain(
      "document_plain.png"), DocumentPlainYellow("document_plain_yellow.png"), DocumentX2(
      "document_x2.png"), DocumentsGear("documents_gear.png"), Donate("donate.png"), Download(
      "download.png"), Earth("earth.png"), EditComponent("edit_component.png"), ElementInto(
      "element_into.png"), Elements("elements1.png"), ElementsSelection(
      "elements_selection.png"), Error("error.png"), Excel("excel.png"), Exit("exit.png"), Export(
      "export.png"), Eye("eye.png"), Faq("faq.png"), Find("find.png"), FitToSize(
      "fit_to_size.png"), FlexibleLeads("flexible_leads.png"), FlipHorizontal(
      "flip_horizontal.png"), FlipVertical("flip_vertical.png"), FolderOut(
      "folder_out.png"), FormGreen("form_green.png"), Front("front.png"), Garbage(
      "garbage.png"), Gears("gears.png"), GraphEdgeDirected("graph_edge_directed.png"), GraphNodes(
      "graph_nodes.png"), Group("group.png"), Guitar("guitar.png"), HTML("html.png"), Hammer(
      "hammer.png"), Help("help2.png"), History("history.png"), IconLarge(
      "icon_large.png"), IconMedium("icon_medium.png"), IconSmall("icon_small.png"), IdCard(
      "id_card.png"), IdCardAdd("id_card_add.png"), IdCardEdit("id_card_edit.png"), Image(
      "image.png"), ImportNetlist("import_netlist.png"), JarBeanInto("jar_bean_into.png"), KeyEdit(
      "key_edit.png"), LaserPointer("laserpointer.png"), LightBulbOff(
      "lightbulb_off.png"), LightBulbOn("lightbulb_on.png"), Loadline("loadline.png"), LoadlineAdd(
      "loadline_add.png"), Lock("lock.png"), MagicWand("magic_wand.png"), Manual(
      "manual.png"), Megaphone("megaphone.png"), Messages("messages.png"), MissingImage(
      "missing_image.png"), MoveSmall("move_small.png"), Multimeter("multimeter.png"), NavLeftBlue(
      "nav_left_blue.png"), NavRightBlue("nav_right_blue.png"), NavigateCheck(
      "navigate_check.png"), Node("node.png"), NotebookAdd("notebook_add.png"), PDF(
      "pdf.png"), Pads("pads.png"), Paste("paste.png"), Patreon("patreon.png"), Pens(
      "pens.png"), PhotoScenery("photo_scenery.png"), PinGreen("pin_green.png"), PinGrey(
      "pin_grey.png"), Plugin("plugin.png"), Print("print.png"), Project("project.png"), RotateCCW(
      "rotate_ccw.png"), RotateCW("rotate_cw.png"), SaveAs("save_as.png"), Scientist(
      "scientist.png"), Screwdriver("screwdriver.png"), ScrollInformation(
      "scroll_information.png"), SearchBox("search-box.png"), Selection(
      "selection.png"), Silkscreen("silkscreen.png"), Size("size.png"), SnapToComponents(
      "snap_to_components.png"), SnapToGrid("snap_to_grid.png"), SnapToNone(
      "snap_to_none.png"), Sort("sort.png"), Spinning("spinning.gif"), Splash(
      "splash.png"), SplashCeramic("splash_ceramic.png"), SplashElectrolytic(
      "splash_electrolytic.png"), SplashFilm("splash_film.png"), SplashIC(
      "splash_ic.png"), SplashResistor("splash_resistor.png"), StarBlue("star_blue.png"), StarGrey(
      "star_grey.png"), Tables("tables.png"), TapeMeasure("tape_measure1.png"), TraceMask(
      "trace_mask.png"), TraceProximity("trace_proximity.png"), Undo("undo.png"), Ungroup(
      "ungroup.png"), Unlock("lock_open.png"), Upload("upload.png"), User("user1.png"), Warning(
      "warning.png"), Web("web.png"), WindowColors("window_colors.png"), WindowGear(
      "window_gear.png"), Wrench("wrench.png"), ZoomSmall("zoom_small.png");

  protected String name;

  private IconLoader(String name) {
    this.name = name;
  }

  public Icon getIcon() {
    java.net.URL imgURL = getClass().getResource("/diylc-swing-images/" + name);
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
      img = ImageIO.read(getClass().getResourceAsStream("/diylc-swing-images/" + name));
    } catch (IOException e) {
      Logger.getLogger(IconLoader.class).error("Couldn't find file: " + name);
    }
    return img;
  }
}
