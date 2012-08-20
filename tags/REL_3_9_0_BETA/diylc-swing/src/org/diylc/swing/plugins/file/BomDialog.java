package org.diylc.swing.plugins.file;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.diylc.images.IconLoader;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.export.TableExporter;
import org.diylc.swingframework.objecttable.ObjectListTable;
import org.diylc.utils.BomEntry;

public class BomDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private ObjectListTable<BomEntry> table;
	private JPanel toolbar;

	public BomDialog(JFrame parent, List<BomEntry> bom) {
		super(parent, "Bill of Materials");
		setContentPane(createMainPanel());
		getTable().setData(bom);
		pack();
		setLocationRelativeTo(parent);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			getTable().autoFit(Arrays.asList(3));
		}
	}

	private JPanel createMainPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(getToolbar(), BorderLayout.NORTH);
		mainPanel.add(new JScrollPane(getTable()), BorderLayout.CENTER);
		return mainPanel;
	}

	private ObjectListTable<BomEntry> getTable() {
		if (table == null) {
			try {
				table = new ObjectListTable<BomEntry>(BomEntry.class, new String[] { "getName",
						"getValue", "getQuantity", "getNotes/setNotes" }, null);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return table;
	}

	private JPanel getToolbar() {
		if (toolbar == null) {
			toolbar = new JPanel();
			toolbar.add(new JButton(new SaveToExcelAction()));
			toolbar.add(new JButton(new SaveToCSVAction()));
			toolbar.add(new JButton(new SaveToPNGAction()));
			toolbar.add(new JButton(new SaveToHTMLAction()));
		}
		return toolbar;
	}

	class SaveToExcelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveToExcelAction() {
			super();
			putValue(Action.NAME, "Save to Excel");
			putValue(Action.SMALL_ICON, IconLoader.Excel.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.EXCEL.getFilter(), null,
					FileFilterEnum.EXCEL.getExtensions()[0], null);
			if (file != null) {
				try {
					TableExporter.getInstance().exportToExcel(getTable(), file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	class SaveToCSVAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveToCSVAction() {
			super();
			putValue(Action.NAME, "Save to CSV");
			putValue(Action.SMALL_ICON, IconLoader.CSV.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.CSV.getFilter(),
					null, FileFilterEnum.CSV.getExtensions()[0], null);
			if (file != null) {
				try {
					TableExporter.getInstance().exportToCSV(getTable(), file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	class SaveToHTMLAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveToHTMLAction() {
			super();
			putValue(Action.NAME, "Save to HTML");
			putValue(Action.SMALL_ICON, IconLoader.HTML.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.HTML.getFilter(),
					null, FileFilterEnum.HTML.getExtensions()[0], null);
			if (file != null) {
				try {
					TableExporter.getInstance().exportToHTML(getTable(), file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	class SaveToPNGAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveToPNGAction() {
			super();
			putValue(Action.NAME, "Save to PNG");
			putValue(Action.SMALL_ICON, IconLoader.Image.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.PNG.getFilter(),
					null, FileFilterEnum.PNG.getExtensions()[0], null);
			if (file != null) {
				try {
					TableExporter.getInstance().exportToPNG(getTable(), file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
