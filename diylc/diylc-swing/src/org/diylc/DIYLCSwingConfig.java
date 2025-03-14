package org.diylc;

import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.presenter.*;
import org.diylc.swing.actions.file.ImportFileView;
import org.diylc.swing.gui.DummyView;
import org.diylc.swing.loadline.LoadlineEditorFrame;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.diylc.config.DIYLCConfig;
import org.diylc.swing.gui.MainFrame;
import org.diylc.core.IView;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.swing.plugins.PluginConfig;

import java.util.List;

@Configuration
//@Import({DIYLCConfig.class})
public class DIYLCSwingConfig {

    @Primary
    @Bean
    public IView mainFrame(@Lazy
                               Presenter presenter, IConfigurationManager configurationManager, List<IPlugIn> plugins) {
        return new MainFrame(presenter, configurationManager, plugins);
    }

    @Bean
    public IView importFileView() {
        return new ImportFileView();
    }

    @Bean
    public IView dummyView() {
        return new DummyView();
    }

    @Bean
    public IView loadlineView(Presenter presenter) {
        return new LoadlineEditorFrame(presenter);
    }
} 