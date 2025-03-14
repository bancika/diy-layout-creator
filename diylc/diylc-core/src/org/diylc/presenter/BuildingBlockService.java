package org.diylc.presenter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.diylc.common.IBlockProcessor;
import org.diylc.core.IDIYComponent;

public interface BuildingBlockService {
    // Core block operations
    void saveSelectionAsBlock(String blockName, Collection<IDIYComponent<?>> selectedComponents, 
            List<IDIYComponent<?>> allComponents);
    List<IDIYComponent<?>> loadBlock(String blockName, List<IDIYComponent<?>> existingComponents) 
            throws IBlockProcessor.InvalidBlockException;
    void deleteBlock(String blockName);
    
    // Import/Export
    void importDefaultBlocks();
    int importBlocks(String fileName) throws IOException;
} 