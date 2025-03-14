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
public class DummyViewDIYLCConfig {

    @Bean
    public IConfigurationManager dummyViewConfigManager() {
        return InMemoryConfigurationManager.getInstance();
    }

    @Bean
    public MessageDispatcher<EventType> dummyViewMessageDispatcher() {
        return new MessageDispatcher<>(true);
    }

    @Bean
    public DrawingService dummyViewDrawingService(
            @Qualifier("dummyViewMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("dummyViewConfigManager")
            IConfigurationManager configManager) {
        return new DrawingManager(messageDispatcher, configManager);
    }

    @Bean
    public ProjectFileService dummyViewProjectFileService(MessageDispatcher<EventType> messageDispatcher) {
        return new ProjectFileManager(messageDispatcher);
    }

    @Bean
    public InstantiationService dummyViewInstantiationService() {
        return new InstantiationManager();
    }

    @Bean
    public VariantService dummyViewVariantService(
            @Qualifier("dummyViewConfigManager")
            IConfigurationManager configManager,
            @Qualifier("dummyViewProjectFileService")
            ProjectFileService projectFileService) {
        return new VariantManager(configManager, projectFileService.getXStream());
    }

    @Bean
    public BuildingBlockService dummyViewBuildingBlockService(
            @Qualifier("dummyViewConfigManager")
            IConfigurationManager configManager,
            @Qualifier("dummyViewProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("dummyViewInstantiationService")
            InstantiationService instantiationService) {
        return new BuildingBlockManager(configManager, projectFileService.getXStream(), instantiationService);
    }

    @Bean
    public Presenter dummyViewPresenter(
            @Qualifier("dummyView")
            IView view,
            @Qualifier("dummyViewConfigManager")
            IConfigurationManager configManager,
            @Qualifier("dummyViewMessageDispatcher")
            MessageDispatcher<EventType> messageDispatcher,
            @Qualifier("dummyViewDrawingService")
            DrawingService drawingService,
            @Qualifier("dummyViewProjectFileService")
            ProjectFileService projectFileService,
            @Qualifier("dummyViewInstantiationService")
            InstantiationService instantiationService,
            @Qualifier("dummyViewVariantService")
            VariantService variantService,
            @Qualifier("dummyViewBuildingBlockService")
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