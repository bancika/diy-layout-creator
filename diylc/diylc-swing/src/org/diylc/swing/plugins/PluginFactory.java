package org.diylc.swing.plugins;

import org.diylc.swing.ISwingUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.swing.gui.*;
import org.diylc.swing.plugins.canvas.CanvasPlugin;
import org.diylc.swing.plugins.file.FileMenuPlugin;
import org.diylc.swing.plugins.edit.EditMenuPlugin;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.layers.LayersMenuPlugin;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.swing.plugins.help.HelpMenuPlugin;
import org.diylc.swing.plugins.test.TestMenuPlugin;
import org.diylc.swing.plugins.statusbar.StatusBar;
import org.diylc.swing.plugins.tree.ComponentTree;
import org.diylc.swing.plugins.explorer.ExplorerPlugin;
import org.diylc.swing.plugins.toolbox.ToolBox;
import org.diylc.swing.gui.actionbar.ActionBarPlugin;

import javax.swing.*;

@Component
public class PluginFactory {
    
    private final IConfigurationManager configManager;

    @Autowired
    public PluginFactory(IConfigurationManager configManager) {
        this.configManager = configManager;
    }

    public CanvasPlugin createCanvasPlugin(ISwingUI ui) {
        return new CanvasPlugin(ui, configManager);
    }

    public FileMenuPlugin createFileMenuPlugin(ISwingUI ui) {
        return new FileMenuPlugin(ui);
    }

    public EditMenuPlugin createEditMenuPlugin(ISwingUI ui) {
        return new EditMenuPlugin(ui);
    }

    public ConfigPlugin createConfigPlugin(ISwingUI ui) {
        return new ConfigPlugin(ui);
    }

    public LayersMenuPlugin createLayersMenuPlugin(ISwingUI ui) {
        return new LayersMenuPlugin(ui);
    }

    public CloudPlugIn createCloudPlugIn(ISwingUI ui) {
        return new CloudPlugIn(ui);
    }

    public TestMenuPlugin createTestMenuPlugin(ISwingUI ui) {
        return new TestMenuPlugin(ui);
    }

    public HelpMenuPlugin createHelpMenuPlugin(ISwingUI ui) {
        return new HelpMenuPlugin(ui);
    }

    public ActionBarPlugin createActionBarPlugin(ISwingUI ui) {
        return new ActionBarPlugin(ui);
    }

    public StatusBar createStatusBar(ISwingUI ui) {
        return new StatusBar(ui);
    }

    public ToolBox createToolBox(ISwingUI ui) {
        return new ToolBox(ui);
    }

    public ComponentTree createComponentTree(ISwingUI ui, JComponent canvasPanel) {
        return new ComponentTree(ui, canvasPanel);
    }

    public ExplorerPlugin createExplorerPlugin(ISwingUI ui, JComponent canvasPanel) {
        return new ExplorerPlugin(ui, canvasPanel);
    }
} 