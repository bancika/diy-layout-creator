package org.diylc.config;

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

@Configuration
public class LoadlineDIYLCConfig {

    @Bean
    public IConfigurationManager loadlineConfigManager() {
        return InMemoryConfigurationManager.getInstance();
    }

    @Bean
    public MessageDispatcher<EventType> loadlineMessageDispatcher() {
        return new MessageDispatcher<>(true);
    }

    @Bean
    public DrawingService loadlineDrawingService(
            @Qualifier("loadlineMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("loadlineConfigManager")
            IConfigurationManager configManager) {
        return new DrawingManager(messageDispatcher, configManager);
    }

    @Bean
    public ProjectFileService loadlineProjectFileService(MessageDispatcher<EventType> messageDispatcher) {
        return new ProjectFileManager(messageDispatcher);
    }

    @Bean
    public InstantiationService loadlineInstantiationService() {
        return new InstantiationManager();
    }

    @Bean
    public VariantService loadlineVariantService(
            @Qualifier("loadlineConfigManager")
            IConfigurationManager configManager,
            @Qualifier("loadlineProjectFileService")
            ProjectFileService projectFileService) {
        return new VariantManager(configManager, projectFileService.getXStream());
    }

    @Bean
    public BuildingBlockService loadlineBuildingBlockService(
            @Qualifier("loadlineConfigManager")
            IConfigurationManager configManager,
            @Qualifier("loadlineProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("loadlineInstantiationService")
            InstantiationService instantiationService) {
        return new BuildingBlockManager(configManager, projectFileService.getXStream(), instantiationService);
    }

    @Bean
    public Presenter loadlinePresenter(
            @Qualifier("loadlineView")
            IView view,
            @Qualifier("loadlineConfigManager")
            IConfigurationManager configManager,
            @Qualifier("loadlineMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("loadlineDrawingService")
            DrawingService drawingService,
            @Qualifier("loadlineProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("loadlineInstantiationService")
            InstantiationService instantiationService,
            @Qualifier("loadlineVariantService")
            VariantService variantService,
            @Qualifier("loadlineBuildingBlockService")
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