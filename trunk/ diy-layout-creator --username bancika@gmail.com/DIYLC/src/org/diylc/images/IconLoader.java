package org.diylc.images;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Loads image resources as Icons.
 * 
 * @author Branislav Stojkovic
 */
public enum IconLoader {

	Delete("delete.png"), Add("add.png"), FolderOut("folder_out.png"), Garbage("garbage.png"), DiskBlue(
			"disk_blue.png"), SaveAs("save_as.png"), Exit("exit.png"), DocumentPlainYellow(
			"document_plain_yellow.png"), PhotoScenery("photo_scenery.png"), LightBulbOn(
			"lightbulb_on.png"), LightBulbOff("lightbulb_off.png"), NotebookAdd("notebook_add.png"), FormGreen(
			"form_green.png"), Gears("gears.png"), About("about.png"), WindowColors(
			"window_colors.png"), WindowGear("window_gear.png"), NavigateCheck("navigate_check.png"), Undo(
			"undo.png"), Error("error.png"), Warning("warning.png"), ZoomSmall("zoom_small.png"), MoveSmall(
			"move_small.png"), Print("print.png"), PDF("pdf.png"), Excel("excel.png"), CSV(
			"csv.png"), HTML("html.png"), Image("image.png"), Cut("cut.png"), Copy("copy.png"), Paste(
			"paste.png"), Selection("selection.png"), BOM("bom.png"), BlackBoard("blackboard.png"), IdCard(
			"id_card.png"), IdCardAdd("id_card_add.png"), Chest("chest.png"), Upload("upload.png"), Wrench(
			"wrench.png"), Group("group.png"), Ungroup("ungroup.png"), TraceMask("trace_mask.png"), Faq(
			"faq.png"), Component("component.png"), Plugin("plugin.png"), Manual("manual.png"), Donate(
			"donate.png"), Bug("bug.png"), AboutDialog("about_dialog.png");

	protected String name;

	private IconLoader(String name) {
		this.name = name;
	}

	public Icon getIcon() {
		java.net.URL imgURL = getClass().getResource(name);
		if (imgURL != null) {
			return new ImageIcon(imgURL, name);
		} else {
			System.err.println("Couldn't find file: " + name);
			return null;
		}
	}
}
