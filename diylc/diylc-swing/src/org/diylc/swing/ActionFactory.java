/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.measures.Nudge;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.images.IconLoader;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.edit.ComponentTransferable;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.CheckBoxListDialog;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.swingframework.text.TextDialog;
import org.diylc.utils.BomEntry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ActionFactory {

  private static final Logger LOG = Logger.getLogger(ActionFactory.class);

  private static ActionFactory instance;

  public static ActionFactory getInstance() {
    if (instance == null) {
      instance = new ActionFactory();
    }
    return instance;
  }

  private ActionFactory() {}

  // File menu actions.

  public NewAction createNewAction(IPlugInPort plugInPort) {
    return new NewAction(plugInPort);
  }

  public OpenAction createOpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new OpenAction(plugInPort, swingUI);
  }

  public ImportAction createImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new ImportAction(plugInPort, swingUI);
  }

  public SaveAction createSaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new SaveAction(plugInPort, swingUI);
  }

  public SaveAsAction createSaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new SaveAsAction(plugInPort, swingUI);
  }

  public CreateBomAction createBomAction(IPlugInPort plugInPort) {
    return new CreateBomAction(plugInPort);
  }

  public ExportPDFAction createExportPDFAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
    return new ExportPDFAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
  }

  public ExportPNGAction createExportPNGAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
    return new ExportPNGAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
  }

  public PrintAction createPrintAction(IDrawingProvider drawingProvider, int keyModifiers) {
    return new PrintAction(drawingProvider, keyModifiers);
  }
  
  public ExportVariantsAction createExportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ExportVariantsAction(swingUI, plugInPort);
  }
  
  public ImportVariantsAction createImportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ImportVariantsAction(swingUI, plugInPort);
  }
  
  public ExportBlocksAction createExportBlocksAction(ISwingUI swingUI) {
    return new ExportBlocksAction(swingUI);
  }
  
  public ImportBlocksAction createImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ImportBlocksAction(swingUI, plugInPort);
  }

  public ExitAction createExitAction(IPlugInPort plugInPort) {
    return new ExitAction(plugInPort);
  }

  // Edit menu actions.

  public CutAction createCutAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
    return new CutAction(plugInPort, clipboard, clipboardOwner);
  }

  public CopyAction createCopyAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
    return new CopyAction(plugInPort, clipboard, clipboardOwner);
  }

  public PasteAction createPasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
    return new PasteAction(plugInPort, clipboard);
  }

  public DuplicateAction createDuplicateAction(IPlugInPort plugInPort) {
    return new DuplicateAction(plugInPort);
  }

  public SelectAllAction createSelectAllAction(IPlugInPort plugInPort) {
    return new SelectAllAction(plugInPort);
  }

  public GroupAction createGroupAction(IPlugInPort plugInPort) {
    return new GroupAction(plugInPort);
  }

  public UngroupAction createUngroupAction(IPlugInPort plugInPort) {
    return new UngroupAction(plugInPort);
  }

  public EditProjectAction createEditProjectAction(IPlugInPort plugInPort) {
    return new EditProjectAction(plugInPort);
  }

  public EditSelectionAction createEditSelectionAction(IPlugInPort plugInPort) {
    return new EditSelectionAction(plugInPort);
  }

  public DeleteSelectionAction createDeleteSelectionAction(IPlugInPort plugInPort) {
    return new DeleteSelectionAction(plugInPort);
  }

  public SaveAsTemplateAction createSaveAsTemplateAction(IPlugInPort plugInPort) {
    return new SaveAsTemplateAction(plugInPort);
  }

  public SaveAsBlockAction createSaveAsBlockAction(IPlugInPort plugInPort) {
    return new SaveAsBlockAction(plugInPort);
  }

  public ExpandSelectionAction createExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
    return new ExpandSelectionAction(plugInPort, expansionMode);
  }

  public RotateSelectionAction createRotateSelectionAction(IPlugInPort plugInPort, int direction) {
    return new RotateSelectionAction(plugInPort, direction);
  }

  public MirrorSelectionAction createMirrorSelectionAction(IPlugInPort plugInPort, int direction) {
    return new MirrorSelectionAction(plugInPort, direction);
  }

  public SendToBackAction createSendToBackAction(IPlugInPort plugInPort) {
    return new SendToBackAction(plugInPort);
  }

  public BringToFrontAction createBringToFrontAction(IPlugInPort plugInPort) {
    return new BringToFrontAction(plugInPort);
  }

  public NudgeAction createNudgeAction(IPlugInPort plugInPort) {
    return new NudgeAction(plugInPort);
  }

  // Config actions.

  public ConfigAction createConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
    return new ConfigAction(plugInPort, title, configKey, defaultValue);
  }

  public ConfigAction createConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue,
      String tipKey) {
    return new ConfigAction(plugInPort, title, configKey, defaultValue, tipKey);
  }

  public ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
    return new ThemeAction(plugInPort, theme);
  }

  public ComponentBrowserAction createComponentBrowserAction(String browserType) {
    return new ComponentBrowserAction(browserType);
  }

  public RenumberAction createRenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
    return new RenumberAction(plugInPort, xAxisFirst);
  }
  
  public GenerateNetlistAction createGenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new GenerateNetlistAction(plugInPort, swingUI);
  }
  
  public SummarizeNetlistAction createSummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI, INetlistAnalyzer summarizer) {
    return new SummarizeNetlistAction(plugInPort, swingUI, summarizer);
  }

  // File menu actions.

  public static class NewAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public NewAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "New");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentPlain.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("NewAction triggered");
      if (!plugInPort.allowFileAction()) {
        return;
      }
      plugInPort.createNewProject();
      List<PropertyWrapper> properties = plugInPort.getProperties(plugInPort.getCurrentProject());
      PropertyEditorDialog editor =
          DialogFactory.getInstance().createPropertyEditorDialog(properties, "Edit Project", true);
      editor.setVisible(true);
      if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
        plugInPort.applyProperties(plugInPort.getCurrentProject(), properties);
      }
      // Save default values.
      for (PropertyWrapper property : editor.getDefaultedProperties()) {
        if (property.getValue() != null) {
          plugInPort.setDefaultPropertyValue(Project.class, property.getName(), property.getValue());
        }
      }
    }
  }

  public static class OpenAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ISwingUI swingUI;

    public OpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Open");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.FolderOut.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("OpenAction triggered");
      if (!plugInPort.allowFileAction()) {
        return;
      }
      final File file =
          DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(), null,
              FileFilterEnum.DIY.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Opening from " + file.getAbsolutePath());
            plugInPort.loadProjectFromFile(file.getAbsolutePath());
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class ImportAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ISwingUI swingUI;
    private Presenter presenter;

    public ImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      this.presenter = new Presenter(new IView() {

        @Override
        public int showConfirmDialog(String message, String title, int optionType, int messageType) {
          return JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
        }

        @Override
        public void showMessage(String message, String title, int messageType) {
          JOptionPane.showMessageDialog(null, message, title, messageType);
        }

        @Override
        public File promptFileSave() {
          return null;
        }

        @Override
        public boolean editProperties(List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties) {
          return false;
        }
      });
      putValue(AbstractAction.NAME, "Import");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.ElementInto.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportAction triggered");

      final File file =
          DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(), null,
              FileFilterEnum.DIY.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Opening from " + file.getAbsolutePath());
            // Load project in temp presenter
            presenter.loadProjectFromFile(file.getAbsolutePath());
            // Grab all components and paste them into the main
            // presenter
            plugInPort.pasteComponents(presenter.getCurrentProject().getComponents(), false);
            // Cleanup components in the temp presenter, don't need
            // them anymore
            presenter.selectAll(0);
            presenter.deleteSelectedComponents();
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class SaveAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ISwingUI swingUI;

    public SaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Save");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("SaveAction triggered");
      if (plugInPort.getCurrentFileName() == null) {
        final File file =
            DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.DIY.getFilter(), null,
                FileFilterEnum.DIY.getExtensions()[0], null);
        if (file != null) {
          swingUI.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              LOG.debug("Saving to " + file.getAbsolutePath());
              plugInPort.saveProjectToFile(file.getAbsolutePath(), false);
              return null;
            }

            @Override
            public void complete(Void result) {}

            @Override
            public void failed(Exception e) {
              swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
            }
          }, true);
        }
      } else {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Saving to " + plugInPort.getCurrentFileName());
            plugInPort.saveProjectToFile(plugInPort.getCurrentFileName(), false);
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class SaveAsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ISwingUI swingUI;

    public SaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Save As");
      putValue(
          AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
              | ActionEvent.SHIFT_MASK));
      putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("SaveAsAction triggered");
      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.DIY.getFilter(), null,
              FileFilterEnum.DIY.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Saving to " + file.getAbsolutePath());
            plugInPort.saveProjectToFile(file.getAbsolutePath(), false);
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class CreateBomAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public CreateBomAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Create B.O.M.");
      putValue(AbstractAction.SMALL_ICON, IconLoader.BOM.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("CreateBomAction triggered");
      List<BomEntry> bom =
          org.diylc.utils.BomMaker.getInstance().createBom(plugInPort.getCurrentProject().getComponents());
      
      String initialFileName = null;
      String currentFile = plugInPort.getCurrentFileName();
      if (currentFile != null) {
        File cFile = new File(currentFile);        
        initialFileName = cFile.getName().replaceAll("(?i)\\.diy", "") + " BOM";
      }
      
      BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom, initialFileName);
      dialog.setVisible(true);
    }
  }

  public static class ExportPDFAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private ISwingUI swingUI;
    private IPlugInPort plugInPort;
    private String defaultSuffix;

    public ExportPDFAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
      super();
      this.plugInPort = plugInPort;
      this.drawingProvider = drawingProvider;
      this.swingUI = swingUI;
      this.defaultSuffix = defaultSuffix;
      putValue(AbstractAction.NAME, "Export to PDF");
      putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ExportPDFAction triggered");
      
      File initialFile = null;
      String currentFile = plugInPort.getCurrentFileName();
      if (currentFile != null) {
        File cFile = new File(currentFile);        
        initialFile = new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".pdf");
      }
      
      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PDF.getFilter(), initialFile,
              FileFilterEnum.PDF.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Exporting to " + file.getAbsolutePath());
            DrawingExporter.getInstance().exportPDF(ExportPDFAction.this.drawingProvider, file);
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not export to PDF. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class ExportPNGAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private ISwingUI swingUI;
    private IPlugInPort plugInPort;
    private String defaultSuffix;

    public ExportPNGAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
      super();
      this.plugInPort = plugInPort;
      this.drawingProvider = drawingProvider;
      this.drawingProvider = drawingProvider;
      this.swingUI = swingUI;
      this.defaultSuffix = defaultSuffix;
      putValue(AbstractAction.NAME, "Export to PNG");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Image.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ExportPNGAction triggered");
      
      File initialFile = null;
      String currentFile = plugInPort.getCurrentFileName();
      if (currentFile != null) {
        File cFile = new File(currentFile);        
        initialFile = new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".png");
      }
      
      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PNG.getFilter(), initialFile,
              FileFilterEnum.PNG.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Exporting to " + file.getAbsolutePath());
            DrawingExporter.getInstance().exportPNG(ExportPNGAction.this.drawingProvider, file);
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not export to PNG. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class PrintAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;

    public PrintAction(IDrawingProvider drawingProvider, int keyModifiers) {
      super();
      this.drawingProvider = drawingProvider;
      putValue(AbstractAction.NAME, "Print...");
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, keyModifiers));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("PrintAction triggered");
      try {
        DrawingExporter.getInstance().print(this.drawingProvider);
      } catch (PrinterException e1) {
        e1.printStackTrace();
      }
    }
  }
  
  public static class ExportVariantsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ISwingUI swingUI;
    
    private Map<String, ComponentType> typeMap = new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

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
      LOG.info("ExportVariantsAction triggered");
      
      Map<String, List<Template>> selectedVariants;
      
      try {
        Map<String, List<Template>> variantMap =
            (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(IPlugInPort.TEMPLATES_KEY, null);
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
            LOG.warn("Could not find type for: " + className);
        }
        
        Collections.sort(types, new Comparator<ComponentType>() {
  
          @Override
          public int compare(ComponentType o1, ComponentType o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
          }});
        
        CheckBoxListDialog dialog = new CheckBoxListDialog(swingUI.getOwnerFrame(), "Export Variants", types.toArray());
        
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
        LOG.error("Error preparing variants for export", ex);
        swingUI.showMessage("Could not export variants. Please check the log for details", "Export Variants", ISwingUI.ERROR_MESSAGE);
        return;
      }

      final VariantPackage variantPkg = new VariantPackage(selectedVariants, System.getProperty("user.name"));

      File initialFile =
          new File(variantPkg.getOwner() == null ? "variants.xml" : ("variants by "
              + variantPkg.getOwner().toLowerCase() + ".xml"));

      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.XML.getFilter(),
              initialFile, FileFilterEnum.XML.getExtensions()[0], null);

      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Exporting variants to " + file.getAbsolutePath());

            try {
              BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
              XStream xStream = new XStream(new DomDriver());
              xStream.toXML(variantPkg, out);
              out.close();
              LOG.info("Exported variants succesfully");
            } catch (IOException e) {
              LOG.error("Could not export variants", e);
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
            swingUI.showMessage("Could not export variants: " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }
  
  public static class ImportVariantsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ISwingUI swingUI;
    private IPlugInPort plugInPort;

    public ImportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
      super();
      this.swingUI = swingUI;
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Import Variants");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportVariantsAction triggered");

      final File file =
          DialogFactory.getInstance().showOpenDialog(FileFilterEnum.XML.getFilter(),
              null, FileFilterEnum.XML.getExtensions()[0], null, swingUI.getOwnerFrame());

      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Integer>() {

          @Override
          public Integer doInBackground() throws Exception {
            return plugInPort.importVariants(file.getAbsolutePath());
          }

          @Override
          public void complete(Integer result) {
            swingUI.showMessage(result + " variant(s) imported from \"" + file.getName() + "\".", "Success",
                ISwingUI.INFORMATION_MESSAGE);
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not import variants: " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }
  
  public static class ExportBlocksAction extends AbstractAction {

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
      LOG.info("ExportBuildingBlocksAction triggered");
      
      Map<String, Collection<IDIYComponent<?>>> selectedBlocks;
      
      try {
        Map<String, Collection<IDIYComponent<?>>> blocks =
            (Map<String, Collection<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(IPlugInPort.BLOCKS_KEY, null);
        if (blocks == null || blocks.isEmpty()) {
          swingUI.showMessage("No building blocks found to export.", "Error", IView.ERROR_MESSAGE);
          return;
        }
        
        String[] options = blocks.keySet().toArray(new String[0]);
        
        Arrays.sort(options, new Comparator<String>() {
  
          @Override
          public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
          }});
        
        CheckBoxListDialog dialog = new CheckBoxListDialog(swingUI.getOwnerFrame(), "Export Building Blocks", options);
        
        dialog.setVisible(true);      
        
        if (dialog.getSelectedButtonCaption() != "OK")
          return;
        
        Object[] selected = dialog.getSelectedOptions();
        
        if (selected.length == 0) {
          swingUI.showMessage("No building blocks selected for export.", "Error", IView.ERROR_MESSAGE);
          return;      
        }
        
        selectedBlocks = new HashMap<String, Collection<IDIYComponent<?>>>();
        for (Object key : selected) {
          selectedBlocks.put(key.toString(), blocks.get(key));
        }
      } catch (Exception ex) {
        LOG.error("Error preparing building blocks for export", ex);
        swingUI.showMessage("Could not export building blocks. Please check the log for details", "Export Building Blocks", ISwingUI.ERROR_MESSAGE);
        return;
      }
      
      final BuildingBlockPackage variantPkg = new BuildingBlockPackage(selectedBlocks, System.getProperty("user.name"));

      File initialFile =
          new File(variantPkg.getOwner() == null ? "building blocks.xml" : ("building blocks by "
              + variantPkg.getOwner().toLowerCase() + ".xml"));

      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.XML.getFilter(),
              initialFile, FileFilterEnum.XML.getExtensions()[0], null);

      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Exporting variants to " + file.getAbsolutePath());

            try {
              BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
              XStream xStream = new XStream(new DomDriver());
              xStream.toXML(variantPkg, out);
              out.close();
              LOG.info("Exported building blocks succesfully");
            } catch (IOException e) {
              LOG.error("Could not export building blocks", e);
            }

            return null;
          }

          @Override
          public void complete(Void result) {
            swingUI.showMessage("Building blocks exported to \"" + file.getName() + "\".", "Success",
                ISwingUI.INFORMATION_MESSAGE);
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not export building blocks: " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }
  
  public static class ImportBlocksAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ISwingUI swingUI;
    private IPlugInPort plugInPort;

    public ImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
      super();
      this.swingUI = swingUI;
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Import Building Blocks");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ImportBlocksAction triggered");
      

      final File file =
          DialogFactory.getInstance().showOpenDialog(FileFilterEnum.XML.getFilter(),
              null, FileFilterEnum.XML.getExtensions()[0], null, swingUI.getOwnerFrame());

      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Integer>() {

          @Override
          public Integer doInBackground() throws Exception {
            return plugInPort.importBlocks(file.getAbsolutePath());
          }

          @Override
          public void complete(Integer result) {
            swingUI.showMessage(result + " building block(s) imported from \"" + file.getName() + "\".", "Success",
                ISwingUI.INFORMATION_MESSAGE);
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not import building blocks: " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }

  public static class ExitAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public ExitAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Exit");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Exit.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ExitAction triggered");
      if (plugInPort.allowFileAction()) {
        System.exit(0);
      }
    }
  }

  // Edit menu actions.

  public static class CutAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private Clipboard clipboard;
    private ClipboardOwner clipboardOwner;

    public CutAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
      super();
      this.plugInPort = plugInPort;
      this.clipboard = clipboard;
      this.clipboardOwner = clipboardOwner;
      putValue(AbstractAction.NAME, "Cut");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Cut.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Cut triggered");
      clipboard.setContents(new ComponentTransferable(cloneComponents(plugInPort.getSelectedComponents())),
          clipboardOwner);
      plugInPort.deleteSelectedComponents();
    }
  }

  public static class PasteAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private Clipboard clipboard;

    public PasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
      super();
      this.plugInPort = plugInPort;
      this.clipboard = clipboard;
      putValue(AbstractAction.NAME, "Paste");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Paste.getIcon());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Paste triggered");
      try {
        List<IDIYComponent<?>> components =
            (List<IDIYComponent<?>>) clipboard.getData(ComponentTransferable.listFlavor);
        plugInPort.pasteComponents(cloneComponents(components), false);
      } catch (Exception ex) {
        LOG.error("Coule not paste.", ex);
      }
    }
  }

  public static class CopyAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private Clipboard clipboard;
    private ClipboardOwner clipboardOwner;

    public CopyAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
      super();
      this.plugInPort = plugInPort;
      this.clipboard = clipboard;
      this.clipboardOwner = clipboardOwner;
      putValue(AbstractAction.NAME, "Copy");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Copy.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Copy triggered");
      clipboard.setContents(new ComponentTransferable(cloneComponents(plugInPort.getSelectedComponents())),
          clipboardOwner);
    }
  }


  public static class DuplicateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public DuplicateAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Duplicate");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentsGear.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Duplicate triggered");
      try {
        plugInPort.duplicateSelection();
      } catch (Exception ex) {
        LOG.error("Coule not duplicate.", ex);
      }
    }
  }

  private static List<IDIYComponent<?>> cloneComponents(Collection<IDIYComponent<?>> components) {
    List<IDIYComponent<?>> result = new ArrayList<IDIYComponent<?>>(components.size());
    for (IDIYComponent<?> component : components) {
      try {
        result.add(component.clone());
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  public static class SelectAllAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public SelectAllAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Select All");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Selection.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Select All triggered");
      plugInPort.selectAll(0);
    }
  }

  public static class GroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public GroupAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Group Selection");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Group.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Group Selection triggered");
      plugInPort.groupSelectedComponents();
    }
  }

  public static class UngroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public UngroupAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Ungroup Selection");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Ungroup.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Ungroup Selection triggered");
      plugInPort.ungroupSelectedComponents();
    }
  }

  public static class EditProjectAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public EditProjectAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Edit Project Settings");
      putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentEdit.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Edit Project triggered");
      List<PropertyWrapper> properties = plugInPort.getProperties(plugInPort.getCurrentProject());
      PropertyEditorDialog editor =
          DialogFactory.getInstance().createPropertyEditorDialog(properties, "Edit Project", true);
      editor.setVisible(true);
      if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
        plugInPort.applyProperties(plugInPort.getCurrentProject(), properties);
      }
      // Save default values.
      for (PropertyWrapper property : editor.getDefaultedProperties()) {
        if (property.getValue() != null) {
          plugInPort.setDefaultPropertyValue(Project.class, property.getName(), property.getValue());
        }
      }
    }
  }

  public static class NudgeAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public NudgeAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Nudge");
      putValue(AbstractAction.SMALL_ICON, IconLoader.FitToSize.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Nudge triggered");
      Nudge n = new Nudge();
      boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true);
      if (metric) {
        n.setxOffset(new Size(0d, SizeUnit.mm));
        n.setyOffset(new Size(0d, SizeUnit.mm));
      } else {
        n.setxOffset(new Size(0d, SizeUnit.in));
        n.setyOffset(new Size(0d, SizeUnit.in));
      }
      List<PropertyWrapper> properties = plugInPort.getProperties(n);
      PropertyEditorDialog editor =
          DialogFactory.getInstance().createPropertyEditorDialog(properties, "Nudge Selection", false);
      editor.setVisible(true);
      if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
        plugInPort.applyProperties(n, properties);
        plugInPort.nudgeSelection(n.getxOffset(), n.getyOffset(), n.getAffectStuckComponents());
      }
    }
  }

  public static class EditSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public EditSelectionAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Edit Selection");
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(AbstractAction.SMALL_ICON, IconLoader.EditComponent.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Edit Selection triggered");
      List<PropertyWrapper> properties = plugInPort.getMutualSelectionProperties();
      if (properties == null || properties.isEmpty()) {
        LOG.info("Nothing to edit");
        return;
      }
      plugInPort.editSelection();
    }
  }

  public static class DeleteSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public DeleteSelectionAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Delete Selection");
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Delete.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Delete Selection triggered");
      plugInPort.deleteSelectedComponents();
    }
  }

  public static class SaveAsTemplateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public SaveAsTemplateAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Save as Variant");
      putValue(AbstractAction.SMALL_ICON, IconLoader.BriefcaseAdd.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Save as template triggered");
      String templateName =
          JOptionPane.showInputDialog(null, "Variant name:", "Save as Variant", JOptionPane.PLAIN_MESSAGE);
      if (templateName != null && !templateName.trim().isEmpty()) {
        plugInPort.saveSelectedComponentAsVariant(templateName);
      }
    }
  }

  public static class SaveAsBlockAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public SaveAsBlockAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Save as Building Block");
      putValue(AbstractAction.SMALL_ICON, IconLoader.ComponentAdd.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Save as building block triggered");
      String templateName =
          JOptionPane.showInputDialog(null, "Block name:", "Save as Building Block", JOptionPane.PLAIN_MESSAGE);
      if (templateName != null && !templateName.trim().isEmpty()) {
        plugInPort.saveSelectionAsBlock(templateName);
      }
    }
  }

  public static class ExpandSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ExpansionMode expansionMode;

    public ExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
      super();
      this.plugInPort = plugInPort;
      this.expansionMode = expansionMode;
      switch (expansionMode) {
        case ALL:
          putValue(AbstractAction.NAME, "All Connected");
          break;
        case IMMEDIATE:
          putValue(AbstractAction.NAME, "Immediate Only");
          break;
        case SAME_TYPE:
          putValue(AbstractAction.NAME, "Same Type Only");
          break;

        default:
          break;
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Expand Selection triggered: " + expansionMode);
      plugInPort.expandSelection(expansionMode);
    }
  }

  public static class RotateSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private int direction;

    public RotateSelectionAction(IPlugInPort plugInPort, int direction) {
      super();
      this.plugInPort = plugInPort;
      this.direction = direction;
      if (direction > 0) {
        putValue(AbstractAction.NAME, "Rotate Clockwise");
        putValue(AbstractAction.SMALL_ICON, IconLoader.RotateCW.getIcon());
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
      } else {
        putValue(AbstractAction.NAME, "Rotate Counterclockwise");
        putValue(AbstractAction.SMALL_ICON, IconLoader.RotateCCW.getIcon());
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Rotate Selection triggered: " + direction);
      plugInPort.rotateSelection(direction);
    }
  }

  public static class MirrorSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private int direction;

    public MirrorSelectionAction(IPlugInPort plugInPort, int direction) {
      super();
      this.plugInPort = plugInPort;
      this.direction = direction;
      if (direction == IComponentTransformer.HORIZONTAL) {
        putValue(AbstractAction.NAME, "Mirror Horizontally");
        putValue(AbstractAction.SMALL_ICON, IconLoader.FlipHorizontal.getIcon());
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
      } else {
        putValue(AbstractAction.NAME, "Mirror Vertically");
        putValue(AbstractAction.SMALL_ICON, IconLoader.FlipVertical.getIcon());
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK));
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Mirror Selection triggered: " + direction);
      plugInPort.mirrorSelection(direction);
    }
  }

  public static class SendToBackAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public SendToBackAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Send Backward");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Back.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Send to Back triggered");
      plugInPort.sendSelectionToBack();
    }
  }

  public static class BringToFrontAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public BringToFrontAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Bring Forward");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Front.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("Bring to Front triggered");
      plugInPort.bringSelectionToFront();
    }
  }

  public static class ConfigAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private String configKey;
    private String tipKey;

    public ConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue, String tipKey) {
      super();
      this.plugInPort = plugInPort;
      this.configKey = configKey;
      this.tipKey = tipKey;
      putValue(AbstractAction.NAME, title);
      putValue(IView.CHECK_BOX_MENU_ITEM, true);
      putValue(AbstractAction.SELECTED_KEY, ConfigurationManager.getInstance().readBoolean(configKey, defaultValue));
    }

    public ConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
      this(plugInPort, title, configKey, defaultValue, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      ConfigurationManager.getInstance().writeValue(configKey, getValue(AbstractAction.SELECTED_KEY));
      if ((Boolean) getValue(AbstractAction.SELECTED_KEY) && tipKey != null
          && !ConfigurationManager.getInstance().readBoolean(tipKey + ".dismissed", false)) {
        DialogFactory.getInstance().createInfoDialog(tipKey).setVisible(true);
      }
      plugInPort.refresh();
    }
  }

  public static class ThemeAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private Theme theme;

    public ThemeAction(IPlugInPort plugInPort, Theme theme) {
      super();
      this.plugInPort = plugInPort;
      this.theme = theme;
      putValue(AbstractAction.NAME, theme.getName());
      putValue(IView.RADIO_BUTTON_GROUP_KEY, "theme");
      putValue(AbstractAction.SELECTED_KEY, plugInPort.getSelectedTheme().getName().equals(theme.getName()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      plugInPort.setSelectedTheme(theme);
    }
  }

  public static class ComponentBrowserAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private String browserType;

    public ComponentBrowserAction(String browserType) {
      super();
      this.browserType = browserType;
      putValue(AbstractAction.NAME, browserType);
      putValue(IView.RADIO_BUTTON_GROUP_KEY, "componentBrowser");

      putValue(
          AbstractAction.SELECTED_KEY,
          browserType.equals(ConfigurationManager.getInstance().readString(ConfigPlugin.COMPONENT_BROWSER,
              ConfigPlugin.SEARCHABLE_TREE)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      ConfigurationManager.getInstance().writeValue(ConfigPlugin.COMPONENT_BROWSER, browserType);
    }
  }


  public static class RenumberAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private boolean xAxisFirst;

    public RenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
      super();
      this.plugInPort = plugInPort;
      this.xAxisFirst = xAxisFirst;
      putValue(AbstractAction.NAME, xAxisFirst ? "Top-to-Bottom" : "Left-to-Right");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      plugInPort.renumberSelectedComponents(xAxisFirst);
    }
  }
  
  public static class GenerateNetlistAction extends AbstractAction {
    
    private static final long serialVersionUID = 1L;
    
    private IPlugInPort plugInPort;
    private ISwingUI swingUI;

    public GenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Create Netlist (beta)");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Web.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<Netlist> res = plugInPort.extractNetlists();
      if (res == null) {
        swingUI.showMessage("The generated netlist is empty, nothing to show.", "Netlist", ISwingUI.INFORMATION_MESSAGE);
        return;
      }
      StringBuilder sb = new StringBuilder("<html>");
      
      for (Netlist netlist : res) {        
        sb.append("<p style=\"font-family: " + new JLabel().getFont().getName() + "; font-size: 9px\"><b>Switch configuration: ").append(netlist.getSwitchSetup()).append("</b><br><br>Connected node groups:<br>");        
        for (Group v : netlist.getSortedGroups()) {
          sb.append("&nbsp;&nbsp;").append(v.getSortedNodes()).append("<br>");          
        }
        sb.append("</p><br><hr>");
      }
      sb.append("</html>");
      new TextDialog(swingUI.getOwnerFrame().getRootPane(), sb.toString(), "Netlist", new Dimension(600, 480)).setVisible(true);
    }    
  }
  
  public static class SummarizeNetlistAction extends AbstractAction {
    
    private static final long serialVersionUID = 1L;
    
    private IPlugInPort plugInPort;
    private ISwingUI swingUI;
    private INetlistAnalyzer summarizer;

    public SummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI, INetlistAnalyzer summarizer) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      this.summarizer = summarizer;
      putValue(AbstractAction.NAME, "Analyze " + summarizer.getName() + " (beta)");      
      putValue(AbstractAction.SMALL_ICON, Enum.valueOf(IconLoader.class, summarizer.getIconName()).getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<Netlist> netlists = plugInPort.extractNetlists();
      if (netlists == null || netlists.isEmpty()) {
        swingUI.showMessage("The generated netlist is empty, nothing to show.", "Analyze " + summarizer.getName(), ISwingUI.INFORMATION_MESSAGE);
        return;
      }
      
      List<Summary> res = null;
      try {
        res = summarizer.summarize(netlists, null);
      } catch (Exception ex) {
        swingUI.showMessage(ex.getMessage(), "Analyze " + summarizer.getName(), ISwingUI.ERROR_MESSAGE);
        return;
      }
      
      if (res == null) {
        swingUI.showMessage("The generated summary is empty, nothing to show.", "Analyze " + summarizer.getName(), ISwingUI.INFORMATION_MESSAGE);
        return;
      }
      StringBuilder sb = new StringBuilder("<html>");
      
      for (Summary summary : res) {        
        sb.append("<p style=\"font-family: " + new JLabel().getFont().getName() + "; font-size: 9px\"><b>Switch configuration: ").append(summary.getNetlist().getSwitchSetup()).append("</b><br><br>");        
        
        sb.append("Parallel/Series connectivity tree:<br><br>").append(summary.getTree().toHTML(0));
        if (!summary.getNotes().isEmpty())
          sb.append("<br><br>Notes:<br>");
        for (String v : summary.getNotes()) {
          sb.append("&nbsp;&nbsp;").append(v).append("<br>");          
        }
        sb.append("</p><br><hr>");
      }
      sb.append("</html>");
      new TextDialog(swingUI.getOwnerFrame().getRootPane(), sb.toString(), "Analyze " + summarizer.getName(), new Dimension(600, 480)).setVisible(true);
    }    
  }
}
