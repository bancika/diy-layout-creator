package org.diylc.presenter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.diylc.common.ComponentType;
import org.diylc.core.Template;

public interface VariantService {
    void importDefaultVariants();
    void upgradeVariants(Map<String, List<ComponentType>> componentTypes);
    List<Template> getVariantsFor(ComponentType type);
    int importVariants(String fileName) throws IOException;
    String getDefaultVariant(ComponentType type);
    void deleteVariant(ComponentType type, String templateName);
} 