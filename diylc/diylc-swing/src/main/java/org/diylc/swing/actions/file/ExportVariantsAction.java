package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.VariantPackage;
import org.diylc.core.IView;
import org.diylc.core.Template;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.CheckBoxListDialog;

public class ExportVariantsAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private ISwingUI swingUI;

  private Map<String, ComponentType> typeMap =
      new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

  public ExportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Export Variants");

    Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet())
      for (ComponentType type : entry.getValue()) {
        typeMap.put(type.getInstanceClass().getCanonicalName(), type);
      }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportVariantsAction triggered");

    Map<String, List<Template>> selectedVariants;

    try {
      Map<String, List<Template>> variantMap = (Map<String, List<Template>>) ConfigurationManager
          .getInstance().readObject(IPlugInPort.TEMPLATES_KEY, null);
      if (variantMap == null || variantMap.isEmpty()) {
        swingUI.showMessage("No variants found to export.", "Error", IView.ERROR_MESSAGE);
        return;
      }

      List<ComponentType> types = new ArrayList<ComponentType>();
      for (String className : variantMap.keySet()) {
        ComponentType type = typeMap.get(className);
        if (type != null)
          types.add(type);
        else
          ActionFactory.LOG.warn("Could not find type for: " + className);
      }

      Collections.sort(types, new Comparator<ComponentType>() {

        @Override
        public int compare(ComponentType o1, ComponentType o2) {
          return o1.toString().compareToIgnoreCase(o2.toString());
        }
      });

      CheckBoxListDialog dialog =
          new CheckBoxListDialog(swingUI.getOwnerFrame(), "Export Variants", types.toArray());

      dialog.setVisible(true);

      if (dialog.getSelectedButtonCaption() != "OK")
        return;

      Object[] selected = dialog.getSelectedOptions();

      if (selected.length == 0) {
        swingUI.showMessage("No variants selected for export.", "Error", IView.ERROR_MESSAGE);
        return;
      }

      selectedVariants = new HashMap<String, List<Template>>();
      for (Object key : selected) {
        ComponentType type = (ComponentType) key;
        String clazz = type.getInstanceClass().getCanonicalName();
        List<Template> variants = variantMap.get(clazz);
        if (variants != null)
          selectedVariants.put(clazz, variants);
      }
    } catch (Exception ex) {
      ActionFactory.LOG.error("Error preparing variants for export", ex);
      swingUI.showMessage("Could not export variants. Please check the log for details",
          "Export Variants", ISwingUI.ERROR_MESSAGE);
      return;
    }

    final VariantPackage variantPkg =
        new VariantPackage(selectedVariants, System.getProperty("user.name"));

    File initialFile = new File(variantPkg.getOwner() == null ? "variants.xml"
        : ("variants by " + variantPkg.getOwner().toLowerCase() + ".xml"));

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
            ActionFactory.LOG.info("Exported variants succesfully");
          } catch (IOException e) {
            ActionFactory.LOG.error("Could not export variants", e);
          }

          return null;
        }

        @Override
        public void complete(Void result) {
          swingUI.showMessage("Variants exported to \"" + file.getName() + "\".", "Success",
              ISwingUI.INFORMATION_MESSAGE);
        }

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export variants: " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}