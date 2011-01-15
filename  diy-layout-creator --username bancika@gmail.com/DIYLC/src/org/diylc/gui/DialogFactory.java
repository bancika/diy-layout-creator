package org.diylc.gui;

import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.diylc.common.PropertyWrapper;
import org.diylc.gui.components.OverwritePromptFileChooser;
import org.diylc.gui.editor.PropertyEditorDialog;
import org.diylc.plugins.file.BomDialog;
import org.diylc.plugins.file.BomEntry;
import org.diylc.plugins.online.view.LoginDialog;
import org.diylc.plugins.online.view.NewUserDialog;
import org.diylc.plugins.online.view.UploadDialog;

import com.diyfever.gui.AboutDialog;
import com.diyfever.gui.IFileChooserAccessory;
import com.diyfever.gui.ProgressDialog;
import com.diyfever.gui.miscutils.ConfigurationManager;

public class DialogFactory {

	private static DialogFactory instance;

	private static final String PATH_KEY = "lastPath";

	public static DialogFactory getInstance() {
		if (instance == null) {
			instance = new DialogFactory();
		}
		return instance;
	}

	private JFrame mainFrame;
	private File lastDirectory;

	private DialogFactory() {
	}

	/**
	 * Sets the frame to be used as dialog parent. This should be called prior
	 * to any other methods in this class.
	 * 
	 * @param mainFrame
	 */
	public void initialize(JFrame mainFrame) {
		this.mainFrame = mainFrame;
		String lastDirectoryPath = (String) ConfigurationManager.getInstance()
				.readString(PATH_KEY, null);
		if (lastDirectoryPath != null) {
			lastDirectory = new File(lastDirectoryPath);
		}
	}

	public PropertyEditorDialog createPropertyEditorDialog(List<PropertyWrapper> properties,
			String title) {
		PropertyEditorDialog editor = new PropertyEditorDialog(mainFrame, properties, title);
		return editor;
	}

	public BomDialog createBomDialog(List<BomEntry> bom) {
		BomDialog dialog = new BomDialog(mainFrame, bom);
		return dialog;
	}

	public File showOpenDialog(FileFilter fileFilter, File initialFile, String defaultExtension,
			IFileChooserAccessory accessory) {
		JFileChooser openFileChooser = new JFileChooser();
		initializeFileChooser(openFileChooser, fileFilter, initialFile, defaultExtension, accessory);

		int result = openFileChooser.showOpenDialog(mainFrame);

		return processFileChooserResult(result, openFileChooser, defaultExtension);
	}

	public File showSaveDialog(FileFilter fileFilter, File initialFile, String defaultExtension,
			IFileChooserAccessory accessory) {
		JFileChooser saveFileChooser = new OverwritePromptFileChooser();
		initializeFileChooser(saveFileChooser, fileFilter, initialFile, defaultExtension, accessory);

		int result = saveFileChooser.showSaveDialog(mainFrame);

		return processFileChooserResult(result, saveFileChooser, defaultExtension);
	}

	private void initializeFileChooser(JFileChooser fileChooser, FileFilter fileFilter,
			File initialFile, String defaultExtension, IFileChooserAccessory accessory) {
		if (accessory != null) {
			accessory.install(fileChooser);
		}
		for (FileFilter filter : fileChooser.getChoosableFileFilters()) {
			fileChooser.removeChoosableFileFilter(filter);
		}
		if (fileChooser instanceof OverwritePromptFileChooser) {
			((OverwritePromptFileChooser) fileChooser).setFileFilter(fileFilter, defaultExtension);
		} else {
			fileChooser.setFileFilter(fileFilter);
		}
		if (lastDirectory != null) {
			fileChooser.setCurrentDirectory(lastDirectory);
		}
		fileChooser.setSelectedFile(initialFile);
	}

	private File processFileChooserResult(int result, JFileChooser fileChooser,
			String defaultExtension) {
		fileChooser.setAccessory(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			lastDirectory = fileChooser.getCurrentDirectory();
			ConfigurationManager.getInstance().writeValue(PATH_KEY,
					lastDirectory.getAbsolutePath());
			if (fileChooser.getSelectedFile().getAbsolutePath().contains(".")) {
				return fileChooser.getSelectedFile();
			} else {
				return new File(fileChooser.getSelectedFile().getAbsoluteFile() + "."
						+ defaultExtension);
			}
		} else {
			return null;
		}
	}

	public AboutDialog createAboutDialog(String appName, Icon icon, String version, String author,
			String url, String mail, String htmlContent) {
		AboutDialog dialog = new AboutDialog(mainFrame, appName, icon, version, author, url, mail,
				htmlContent);
		return dialog;
	}

	public NewUserDialog createNewUserDialog() {
		NewUserDialog dialog = new NewUserDialog(mainFrame);
		return dialog;
	}

	public LoginDialog createLoginDialog() {
		LoginDialog dialog = new LoginDialog(mainFrame);
		return dialog;
	}

	public UploadDialog createUploadDialog() {
		UploadDialog dialog = new UploadDialog(mainFrame);
		return dialog;
	}

	public ProgressDialog createProgressDialog(String title, String[] buttonCaptions,
			String description, boolean useProgress) {
		ProgressDialog dialog = new ProgressDialog(mainFrame, title, buttonCaptions, description,
				useProgress);
		return dialog;
	}
}
