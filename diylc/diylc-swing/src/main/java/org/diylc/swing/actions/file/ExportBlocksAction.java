package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.swingframework.CheckBoxListDialog;

import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ExportBlocksAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private ISwingUI swingUI;

  public ExportBlocksAction(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Export Building Blocks");
    // putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportBuildingBlocksAction triggered");

    Map<String, List<IDIYComponent<?>>> selectedBlocks;

    try {
      Map<String, List<IDIYComponent<?>>> blocks =
          (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance()
              .readObject(IPlugInPort.BLOCKS_KEY, null);
      if (blocks == null || blocks.isEmpty()) {
        swingUI.showMessage("No building blocks found to export.", "Error", IView.ERROR_MESSAGE);
        return;
      }

      String[] options = blocks.keySet().toArray(new String[0]);

      Arrays.sort(options, new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });

      CheckBoxListDialog dialog =
          new CheckBoxListDialog(swingUI.getOwnerFrame(), "Export Building Blocks", options);

      dialog.setVisible(true);

      if (dialog.getSelectedButtonCaption() != "OK")
        return;

      Object[] selected = dialog.getSelectedOptions();

      if (selected.length == 0) {
        swingUI.showMessage("No building blocks selected for export.", "Error",
            IView.ERROR_MESSAGE);
        return;
      }

      selectedBlocks = new HashMap<String, List<IDIYComponent<?>>>();
      for (Object key : selected) {
        selectedBlocks.put(key.toString(), blocks.get(key));
      }
    } catch (Exception ex) {
      ActionFactory.LOG.error("Error preparing building blocks for export", ex);
      swingUI.showMessage("Could not export building blocks. Please check the log for details",
          "Export Building Blocks", ISwingUI.ERROR_MESSAGE);
      return;
    }

    final BuildingBlockPackage variantPkg =
        new BuildingBlockPackage(selectedBlocks, System.getProperty("user.name"));

    File initialFile = new File(variantPkg.getOwner() == null ? "building blocks.xml"
        : ("building blocks by " + variantPkg.getOwner().toLowerCase() + ".xml"));

    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.XML.getFilter(), initialFile, FileFilterEnum.XML.getExtensions()[0], null);

    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Exporting variants to " + file.getAbsolutePath());

          try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            ProjectFileManager.xStreamSerializer.toXML(variantPkg, out);
            out.close();
            ActionFactory.LOG.info("Exported building blocks succesfully");
          } catch (IOException e) {
            ActionFactory.LOG.error("Could not export building blocks", e);
          }

          return null;
        }

        @Override
        public void complete(Void result) {
          swingUI.showMessage("Building blocks exported to \"" + file.getName() + "\".",
              "Success", ISwingUI.INFORMATION_MESSAGE);
        }

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export building blocks: " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}