package org.diylc.config;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.core.IView;
import org.diylc.presenter.*;
import org.diylc.serialization.ProjectFileManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.EventType;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
public class DIYLCConfig {

    @Primary
    @Bean
    public IConfigurationManager configurationManager() {
        return ConfigurationManager.getInstance();
    }

    @Primary
    @Bean
    public MessageDispatcher<EventType> messageDispatcher() {
        return new MessageDispatcher<>(true);
    }

    @Primary
    @Bean
    public DrawingService drawingService(
            MessageDispatcher<EventType> messageDispatcher, 
            IConfigurationManager configManager) {
        return new DrawingManager(messageDispatcher, configManager);
    }

    @Primary
    @Bean
    public ProjectFileService projectFileService(MessageDispatcher<EventType> messageDispatcher) {
        return new ProjectFileManager(messageDispatcher);
    }

    @Primary
    @Bean
    public InstantiationService instantiationService() {
        return new InstantiationManager();
    }

    @Primary
    @Bean
    public VariantService variantService(
            IConfigurationManager configManager, 
            ProjectFileService projectFileService) {
        return new VariantManager(configManager, projectFileService.getXStream());
    }

    @Bean
    public BuildingBlockService buildingBlockService(
            IConfigurationManager configManager,
            ProjectFileService projectFileService,
            InstantiationService instantiationService) {
        return new BuildingBlockManager(configManager, projectFileService.getXStream(), instantiationService);
    }

    @Primary
    @Bean
    public Presenter presenter(
            @Lazy
            IView view,
            IConfigurationManager configManager,
            MessageDispatcher<EventType> messageDispatcher,
            DrawingService drawingService,
            ProjectFileService projectFileService,
            InstantiationService instantiationService,
            VariantService variantService,
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
                true
        );
    }
} 