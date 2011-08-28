package org.diylc.swing.plugins.file;

import java.util.EnumSet;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

/**
 * Entry point class for File management utilities.
 * 
 * @author Branislav Stojkovic
 */
public class FileMenuPlugin implements IPlugIn {

	private static final String FILE_TITLE = "File";
	private static final String TRACE_MASK_TITLE = "Trace Mask";

	private ProjectDrawingProvider drawingProvider;
	private TraceMaskDrawingProvider traceMaskDrawingProvider;

	private ISwingUI swingUI;

	public FileMenuPlugin(ISwingUI swingUI) {
		super();
		this.swingUI = swingUI;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.drawingProvider = new ProjectDrawingProvider(plugInPort, false, true);
		this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

		ActionFactory actionFactory = ActionFactory.getInstance();
		swingUI.injectMenuAction(actionFactory.createNewAction(plugInPort), FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createOpenAction(plugInPort, swingUI), FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createSaveAction(plugInPort, swingUI), FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createSaveAsAction(plugInPort, swingUI), FILE_TITLE);
		swingUI.injectMenuAction(null, FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createExportPDFAction(drawingProvider, swingUI),
				FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createExportPNGAction(drawingProvider, swingUI),
				FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createPrintAction(drawingProvider), FILE_TITLE);
		swingUI.injectSubmenu(TRACE_MASK_TITLE, IconLoader.TraceMask.getIcon(), FILE_TITLE);
		swingUI.injectMenuAction(actionFactory.createExportPDFAction(traceMaskDrawingProvider,
				swingUI), TRACE_MASK_TITLE);
		swingUI.injectMenuAction(actionFactory.createExportPNGAction(traceMaskDrawingProvider,
				swingUI), TRACE_MASK_TITLE);
		swingUI.injectMenuAction(actionFactory.createPrintAction(traceMaskDrawingProvider),
				TRACE_MASK_TITLE);
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
}
