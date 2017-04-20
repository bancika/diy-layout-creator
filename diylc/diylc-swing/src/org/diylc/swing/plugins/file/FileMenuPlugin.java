package org.diylc.swing.plugins.file;

import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.IDynamicSubmenuHandler;
import org.diylc.swing.ISwingUI;

/**
 * Entry point class for File management utilities.
 * 
 * @author Branislav Stojkovic
 */
public class FileMenuPlugin implements IPlugIn, IDynamicSubmenuHandler {

  private static final Logger LOG = Logger.getLogger(FileMenuPlugin.class);

  private static final String FILE_TITLE = "File";
  private static final String TRACE_MASK_TITLE = "Trace Mask";

  private ProjectDrawingProvider drawingProvider;
  private TraceMaskDrawingProvider traceMaskDrawingProvider;

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  public FileMenuPlugin(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    this.drawingProvider = new ProjectDrawingProvider(plugInPort, false, true);
    this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

    ActionFactory actionFactory = ActionFactory.getInstance();
    swingUI.injectMenuAction(actionFactory.createNewAction(plugInPort), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createOpenAction(plugInPort, swingUI), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createImportAction(plugInPort, swingUI), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createSaveAction(plugInPort, swingUI), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createSaveAsAction(plugInPort, swingUI), FILE_TITLE);
    swingUI.injectDynamicSubmenu("Recent Files", IconLoader.History.getIcon(), FILE_TITLE, this);
    swingUI.injectMenuAction(null, FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createExportPDFAction(drawingProvider, swingUI), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createExportPNGAction(drawingProvider, swingUI), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createPrintAction(drawingProvider), FILE_TITLE);
    swingUI.injectSubmenu(TRACE_MASK_TITLE, IconLoader.TraceMask.getIcon(), FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createExportPDFAction(traceMaskDrawingProvider, swingUI), TRACE_MASK_TITLE);
    swingUI.injectMenuAction(actionFactory.createExportPNGAction(traceMaskDrawingProvider, swingUI), TRACE_MASK_TITLE);
    swingUI.injectMenuAction(actionFactory.createPrintAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
    swingUI.injectMenuAction(actionFactory.createBomAction(plugInPort), FILE_TITLE);
    swingUI.injectMenuAction(null, FILE_TITLE);
    swingUI.injectMenuAction(actionFactory.createExitAction(plugInPort), FILE_TITLE);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    // Do nothing.
  }

  // Dynamic menu for recent files

  @Override
  public void onActionPerformed(String name) {
    LOG.info("Openning recent file: " + name);
    if (!plugInPort.allowFileAction()) {
      LOG.info("Aborted openning recent file");
      return;
    }
    this.plugInPort.loadProjectFromFile(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getAvailableItems() {
    return (List<String>) ConfigurationManager.getInstance().readObject(IPlugInPort.RECENT_FILES_KEY, null);
  }
}
