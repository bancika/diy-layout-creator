package org.diylc.swing.plugins.autosave;

import java.io.File;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;

import com.diyfever.gui.miscutils.ConfigurationManager;

public class AutoSavePlugin implements IPlugIn {

	private static final String AUTO_SAVE_FILE_NAME = "autoSave.diy";

	private ExecutorService executor;

	private IPlugInPort plugInPort;
	private IView view;

	public AutoSavePlugin(IView view) {
		this.view = view;
		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				boolean wasAbnormal = ConfigurationManager.getInstance().readBoolean(
						IPlugInPort.ABNORMAL_EXIT_KEY, false);
				if (wasAbnormal && new File(AUTO_SAVE_FILE_NAME).exists()) {
					int decision = view
							.showConfirmDialog(
									"It appears that aplication was not closed normally in the previous session. Do you want to open the last auto-saved file?",
									"Auto-Save", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE);
					if (decision == IView.YES_OPTION) {
						AutoSavePlugin.this.plugInPort.loadProjectFromFile(AUTO_SAVE_FILE_NAME);
					}
				}
				// Set abnormal flag to true, GUI side of the app must flip to
				// false
				// when app closes regularly.
				ConfigurationManager.getInstance().writeValue(IPlugInPort.ABNORMAL_EXIT_KEY, true);
			}
		});
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.PROJECT_MODIFIED);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		if (eventType == EventType.PROJECT_MODIFIED) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					plugInPort.saveProjectToFile(AUTO_SAVE_FILE_NAME, true);
				}
			});
		}
	}
}
