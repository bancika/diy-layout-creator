/*
 * 
 * l * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.presenter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.serialization.ProjectFileManager;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class BuildingBlockManager {

  private static final String BLOCKS_FILE_NAME = "blocks.xml";

  private static final Logger LOG = Logger.getLogger(VariantManager.class);

  private IConfigurationManager<?> configManager;
  private XStream xStream;
  private InstantiationManager instantiationManager;

  public BuildingBlockManager(IConfigurationManager<?> configManager, XStream xStream,
      InstantiationManager instantiationManager) {
    super();
    this.configManager = configManager;
    this.xStream = xStream;
    this.instantiationManager = instantiationManager;
  }

  @SuppressWarnings("unchecked")
  public void importDefaultBlocks() {
    // import default templates from variants.xml file only if we didn't do it already
    if (!configManager.readBoolean(IPlugInPort.DEFAULT_BLOCKS_IMPORTED_KEY, false)) {
      try {
        URL resource = Presenter.class.getResource(BLOCKS_FILE_NAME);
        if (resource != null) {
          BufferedInputStream in = new BufferedInputStream(resource.openStream());
          Map<String, List<IDIYComponent<?>>> defaults =
              (Map<String, List<IDIYComponent<?>>>) ProjectFileManager.xStreamSerializer
                  .fromXML(in);
          in.close();

          Map<String, List<IDIYComponent<?>>> blocksMap =
              (Map<String, List<IDIYComponent<?>>>) configManager.readObject(IPlugInPort.BLOCKS_KEY,
                  null);
          if (blocksMap == null)
            blocksMap = new HashMap<String, List<IDIYComponent<?>>>();

          // merge default blocks with user's
          for (Map.Entry<String, List<IDIYComponent<?>>> entry : defaults.entrySet()) {
            if (!blocksMap.containsKey(entry.getKey()))
              blocksMap.put(entry.getKey(), entry.getValue());
          }

          // update templates and a flag marking that we imported them
          configManager.writeValue(IPlugInPort.DEFAULT_BLOCKS_IMPORTED_KEY, true);
          configManager.writeValue(IPlugInPort.BLOCKS_KEY, blocksMap);
          LOG.info(String.format("Imported %d default building blocks",
              defaults == null ? 0 : defaults.size()));
        }
      } catch (Exception e) {
        LOG.error("Could not load default blocks", e);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public int importBlocks(String fileName) throws IOException {
    LOG.debug(String.format("importBlocks(%s)", fileName));
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));

    BuildingBlockPackage pkg = (BuildingBlockPackage) xStream.fromXML(in);
    
    in.close();

    if (pkg == null || pkg.getBlocks().isEmpty())
      return 0;

    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) configManager.readObject(IBlockProcessor.BLOCKS_KEY, null);
    if (blocks == null) {
      blocks = new HashMap<String, List<IDIYComponent<?>>>();
    }

    for (Map.Entry<String, List<IDIYComponent<?>>> entry : pkg.getBlocks().entrySet()) {
      blocks.put(entry.getKey() + " [" + pkg.getOwner() + "]", entry.getValue());
    }

    configManager.writeValue(IBlockProcessor.BLOCKS_KEY, blocks);

    LOG.info(String.format("Loaded building blocks for %d components", pkg.getBlocks().size()));

    return pkg.getBlocks().size();
  }

  @SuppressWarnings("unchecked")
  public List<IDIYComponent<?>> loadBlock(String blockName,
      List<IDIYComponent<?>> existingComponents) throws InvalidBlockException {
    LOG.debug(String.format("loadBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks = (Map<String, List<IDIYComponent<?>>>) configManager
        .readObject(IPlugInPort.BLOCKS_KEY, null);
    if (blocks != null) {
      Collection<IDIYComponent<?>> components = blocks.get(blockName);
      if (components == null)
        throw new InvalidBlockException();
      // clear potential control point every time!
      instantiationManager.setPotentialControlPoint(null);
      // clone components
      List<IDIYComponent<?>> clones = new ArrayList<IDIYComponent<?>>();
      List<IDIYComponent<?>> testComponents = new ArrayList<IDIYComponent<?>>(existingComponents);
      for (IDIYComponent<?> c : components)
        try {
          IDIYComponent<?> clone = c.clone();
          clone.setName(instantiationManager.createUniqueName(ComponentProcessor.getInstance()
              .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clone.getClass()),
              testComponents));
          testComponents.add(clone);
          clones.add(clone);
        } catch (CloneNotSupportedException e) {
          LOG.error("Could not clone component: " + c);
        }
      return clones;
    } else
      throw new InvalidBlockException();
  }

  @SuppressWarnings("unchecked")
  public void saveSelectionAsBlock(String blockName,
      Collection<IDIYComponent<?>> selectedComponents, List<IDIYComponent<?>> allComponents) {
    LOG.debug(String.format("saveSelectionAsBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks = (Map<String, List<IDIYComponent<?>>>) configManager
        .readObject(IBlockProcessor.BLOCKS_KEY, null);
    if (blocks == null)
      blocks = new HashMap<String, List<IDIYComponent<?>>>();
    List<IDIYComponent<?>> blockComponents = new ArrayList<IDIYComponent<?>>(selectedComponents);
    Collections.sort(blockComponents, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return new Integer(allComponents.indexOf(o1)).compareTo(allComponents.indexOf(o2));
      }
    });
    blocks.put(blockName, blockComponents);

    if (System.getProperty("org.diylc.WriteStaticBlocks", "false").equalsIgnoreCase("true")) {
      Map<String, List<IDIYComponent<?>>> defaultBlockMap =
          new HashMap<String, List<IDIYComponent<?>>>();
      // unify default and user-variants
      for (Map.Entry<String, List<IDIYComponent<?>>> entry : blocks.entrySet()) {
        if (defaultBlockMap.containsKey(entry.getKey())) {
          defaultBlockMap.get(entry.getKey()).addAll(entry.getValue());
        } else {
          defaultBlockMap.put(entry.getKey(), entry.getValue());
        }
      }
      try {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(BLOCKS_FILE_NAME));
        XStream xStream = new XStream(new DomDriver());
        xStream.addPermission(AnyTypePermission.ANY);
        xStream.toXML(defaultBlockMap, out);
        out.close();
        // no more user variants
        configManager.writeValue(IBlockProcessor.BLOCKS_KEY, null);
        LOG.info("Saved default blocks");
      } catch (IOException e) {
        LOG.error("Could not save default blocks", e);
      }
    } else {
      configManager.writeValue(IBlockProcessor.BLOCKS_KEY, blocks);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void deleteBlock(String blockName) {
    LOG.debug(String.format("deleteBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) configManager.readObject(IBlockProcessor.BLOCKS_KEY, null);
    if (blocks != null) {
      blocks.remove(blockName);
      configManager.writeValue(IBlockProcessor.BLOCKS_KEY, blocks);
    }
  }
}
