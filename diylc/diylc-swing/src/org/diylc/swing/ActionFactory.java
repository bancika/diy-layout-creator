package org.diylc.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.measures.Nudge;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.edit.ComponentTransferable;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.utils.BomEntry;

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

  public ExportPDFAction createExportPDFAction(IDrawingProvider drawingProvider, ISwingUI swingUI) {
    return new ExportPDFAction(drawingProvider, swingUI);
  }

  public ExportPNGAction createExportPNGAction(IDrawingProvider drawingProvider, ISwingUI swingUI) {
    return new ExportPNGAction(drawingProvider, swingUI);
  }

  public PrintAction createPrintAction(IDrawingProvider drawingProvider, int keyModifiers) {
    return new PrintAction(drawingProvider, keyModifiers);
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

  public ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
    return new ThemeAction(plugInPort, theme);
  }

  public ComponentBrowserAction createComponentBrowserAction(String browserType) {
    return new ComponentBrowserAction(browserType);
  }

  public RenumberAction createRenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
    return new RenumberAction(plugInPort, xAxisFirst);
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
            plugInPort.pasteComponents(presenter.getCurrentProject().getComponents());
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
      BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom);
      dialog.setVisible(true);
    }
  }

  public static class ExportPDFAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private ISwingUI swingUI;

    public ExportPDFAction(IDrawingProvider drawingProvider, ISwingUI swingUI) {
      super();
      this.drawingProvider = drawingProvider;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Export to PDF");
      putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ExportPDFAction triggered");
      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PDF.getFilter(), null,
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

    public ExportPNGAction(IDrawingProvider drawingProvider, ISwingUI swingUI) {
      super();
      this.drawingProvider = drawingProvider;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Export to PNG");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Image.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("ExportPNGAction triggered");
      final File file =
          DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(), FileFilterEnum.PNG.getFilter(), null,
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
        // TODO Auto-generated catch block
        e1.printStackTrace();
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
        plugInPort.pasteComponents(cloneComponents(components));
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
        plugInPort.saveSelectedComponentAsTemplate(templateName);
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

    public ConfigAction(IPlugInPort plugInPort, String title, String configKey, boolean defaultValue) {
      super();
      this.plugInPort = plugInPort;
      this.configKey = configKey;
      putValue(AbstractAction.NAME, title);
      putValue(IView.CHECK_BOX_MENU_ITEM, true);
      putValue(AbstractAction.SELECTED_KEY, ConfigurationManager.getInstance().readBoolean(configKey, defaultValue));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      ConfigurationManager.getInstance().writeValue(configKey, getValue(AbstractAction.SELECTED_KEY));
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
}
