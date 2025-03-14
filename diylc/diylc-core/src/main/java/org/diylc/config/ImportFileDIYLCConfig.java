package org.diylc.config;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.EventType;
import org.diylc.core.IView;
import org.diylc.presenter.*;
import org.diylc.serialization.ProjectFileManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImportFileDIYLCConfig {

    @Bean
    public IConfigurationManager importFileConfigManager() {
        return InMemoryConfigurationManager.getInstance();
    }

    @Bean
    public MessageDispatcher<EventType> importFileMessageDispatcher() {
        return new MessageDispatcher<>(true);
    }

    @Bean
    public DrawingService importFileDrawingService(
            @Qualifier("importFileMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("importFileConfigManager")
            IConfigurationManager configManager) {
        return new DrawingManager(messageDispatcher, configManager);
    }

    @Bean
    public ProjectFileService importFileProjectFileService(MessageDispatcher<EventType> messageDispatcher) {
        return new ProjectFileManager(messageDispatcher);
    }

    @Bean
    public InstantiationService importFileInstantiationService() {
        return new InstantiationManager();
    }

    @Bean
    public VariantService importFileVariantService(
            @Qualifier("importFileConfigManager")
            IConfigurationManager configManager,
            @Qualifier("importFileProjectFileService")
            ProjectFileService projectFileService) {
        return new VariantManager(configManager, projectFileService.getXStream());
    }

    @Bean
    public BuildingBlockService importFileBuildingBlockService(
            @Qualifier("importFileConfigManager")
            IConfigurationManager configManager,
            @Qualifier("importFileProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("importFileInstantiationService")
            InstantiationService instantiationService) {
        return new BuildingBlockManager(configManager, projectFileService.getXStream(), instantiationService);
    }

    @Bean
    public Presenter importFilePresenter(
            @Qualifier("importFileView")
            IView view,
            @Qualifier("importFileConfigManager")
            IConfigurationManager configManager,
            @Qualifier("importFileMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("importFileDrawingService")
            DrawingService drawingService,
            @Qualifier("importFileProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("importFileInstantiationService")
            InstantiationService instantiationService,
            @Qualifier("importFileVariantService")
            VariantService variantService,
            @Qualifier("importFileBuildingBlockService")
            BuildingBlockService buildingBlockService) {
        return new Presenter(
                view,
                configManager,
                messageDispatcher,
                drawingService,
                projectFileService,
                instantiationService,
                variantService,
                buildingBlockService,
                false
        );
    }
} 