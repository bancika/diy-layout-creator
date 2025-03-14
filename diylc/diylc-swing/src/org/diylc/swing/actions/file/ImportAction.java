package org.diylc.swing.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.diylc.DIYLCSwingConfig;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.clipboard.ComponentTransferableFactory;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.config.DIYLCConfig;
import org.diylc.config.ImportFileDIYLCConfig;
import org.diylc.core.IView;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class ImportAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private Presenter presenter;

  public ImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;

    ConfigurableApplicationContext context = new SpringApplicationBuilder(ImportFileDIYLCConfig.class, DIYLCSwingConfig.class)
            .web(WebApplicationType.NONE)
            .headless(false)
            .run();

    this.presenter = context.getBean(Presenter.class);

    putValue(AbstractAction.NAME, "Import");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.ElementInto.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ImportAction triggered");

    final File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(),
        null, FileFilterEnum.DIY.getExtensions()[0], null);
    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Opening from " + file.getAbsolutePath());
          // Load project in temp presenter
          presenter.loadProjectFromFile(file.getAbsolutePath());
          // Grab all components and paste them into the main
          // presenter
          plugInPort.pasteComponents(ComponentTransferableFactory.getInstance().build(
              presenter.getCurrentProject().getComponents(),
              presenter.getCurrentProject().getGroups()), false, false);
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
          swingUI.showMessage("Could not open file. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}