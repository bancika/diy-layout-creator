package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileFilter;

import org.diylc.swingframework.ButtonDialog;

import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IDIYComponent;
import org.diylc.lang.LangUtil;
import org.diylc.netlist.INetlistParser;
import org.diylc.netlist.ParsedNetlistComponent;
import org.diylc.netlist.ParsedNetlistEntry;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.file.NetlistImportDialog;

public class ImportNetlistAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  private List<INetlistParser> parserDefinitions;
  private List<String> extensions;
  private FileFilter filter;

  public ImportNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Import Netlist");
    putValue(AbstractAction.SMALL_ICON, IconLoader.ImportNetlist.getIcon());

    this.parserDefinitions = plugInPort.getNetlistParserDefinitions();
    this.extensions =
        parserDefinitions.stream().map(x -> x.getFileExt()).collect(Collectors.toList());

    this.filter = new FileFilter() {

      @Override
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }
        String fileExt = f.getName();
        fileExt = fileExt.substring(fileExt.lastIndexOf('.') + 1).toLowerCase();
        for (String ext : extensions) {
          if (ext.equals(fileExt)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public String getDescription() {
        List<String> ext = extensions.stream().map(ex -> "*." + ex).collect(Collectors.toList());
        return LangUtil.translate("Netlist files") + " (" + String.join(",", ext) + ")";
      }
    };
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ImportNetlistAction triggered");

    final File file = DialogFactory.getInstance().showOpenDialog(this.filter, null,
        this.extensions.get(0), null);
    // TODO: identity by extension
    final INetlistParser parser = parserDefinitions.get(0);

    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<List<ParsedNetlistEntry>>() {

        @Override
        public List<ParsedNetlistEntry> doInBackground() throws Exception {
          ActionFactory.LOG.debug("Importing netlist from " + file.getAbsolutePath());
          List<String> outputWarnings = new ArrayList<String>();
          List<ParsedNetlistEntry> entries =
              parser.parseFile(file.getAbsolutePath(), outputWarnings);
          if (!outputWarnings.isEmpty())
            ActionFactory.LOG.warn("Parsing produced warnings:\n" + String.join("\n", outputWarnings));
          return entries;
        }

        @Override
        public void complete(List<ParsedNetlistEntry> entries) {
          try {
            NetlistImportDialog dialog =
                DialogFactory.getInstance().createNetlistImportDialog(plugInPort, entries);
            dialog.setVisible(true);
            if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
              List<String> outputWarnings = new ArrayList<String>();
              Map<String, Class<?>> results = dialog.getResults();
              List<ParsedNetlistComponent> parsedComponents = entries.stream()
                  .map(entry -> new ParsedNetlistComponent(results.get(entry.getRawType()),
                      entry.getValues()))
                  .collect(Collectors.toList());
              List<IDIYComponent<?>> components =
                  parser.generateComponents(parsedComponents, outputWarnings);
              if (!outputWarnings.isEmpty())
                ActionFactory.LOG.warn("Component creation produced warnings:\n"
                    + String.join("\n", outputWarnings));
              plugInPort.pasteComponents(new ComponentTransferable(components), false, false);
            }
          } catch (Exception e) {
            swingUI.showMessage("Could not import netlist file: " + e.getMessage(), "Error",
                ISwingUI.ERROR_MESSAGE);
            e.printStackTrace();
          }
        }

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not import netlist file: " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}