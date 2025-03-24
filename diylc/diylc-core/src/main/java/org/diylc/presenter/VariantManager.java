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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigurationManager;

import com.thoughtworks.xstream.XStream;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.VariantPackage;
import org.diylc.core.Template;
import org.diylc.serialization.ProjectFileManager;

public class VariantManager {

  private static final String VARIANTS_FILE_NAME = "import-defaults/variants.xml";

  private static final Logger LOG = Logger.getLogger(VariantManager.class);

  private IConfigurationManager<?> configManager;
  private XStream xStream;

  public VariantManager(IConfigurationManager<?> configManager, XStream xStream) {
    super();
    this.configManager = configManager;
    this.xStream = xStream;
  }

  @SuppressWarnings("unchecked")
  public void importDefaultVariants() {
    // import default templates from variants.xml file only if we didn't do it already
    LocalDateTime importDate = (LocalDateTime) configManager
        .readObject(IPlugInPort.DEFAULT_TEMPLATES_IMPORT_DATE_KEY, LocalDateTime.MIN);
    
    int importCount = 0;

    try {
      InputStream inputStream = Presenter.class.getResourceAsStream("/" + VARIANTS_FILE_NAME);
      if (inputStream == null) {
        return;
      }
      BufferedInputStream in = new BufferedInputStream(inputStream);
      Map<String, List<Template>> defaults =
          (Map<String, List<Template>>) ProjectFileManager.xStreamSerializer.fromXML(in);
      in.close();

      Map<String, List<Template>> variantMap =
          (Map<String, List<Template>>) configManager.readObject(IPlugInPort.TEMPLATES_KEY, null);
      if (variantMap == null)
        variantMap = new HashMap<String, List<Template>>();

      // merge default variants with user's
      for (Map.Entry<String, List<Template>> entry : defaults.entrySet()) {
        List<Template> existingVariants = variantMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<Template>());
        Set<String> existingNames = existingVariants.stream()
          .map(x -> x.getName())
          .collect(Collectors.toSet());

        for (Template variant : entry.getValue()) {
          if ((variant.getCreatedOn() == null || importDate.compareTo(variant.getCreatedOn()) < 0)
              && !existingNames.contains(variant.getName())) {
            existingVariants.add(variant);
            importCount++;
          }
        }
      }

      // update templates and a flag marking that we imported them
      configManager.writeValue(IPlugInPort.DEFAULT_TEMPLATES_IMPORT_DATE_KEY,
          LocalDateTime.now());
      configManager.writeValue(IPlugInPort.TEMPLATES_KEY, variantMap);
      LOG.info(String.format("Imported default %d variants", importCount));
    } catch (Exception e) {
      LOG.error("Could not load default variants", e);
    }
  }

  private static boolean upgradedVariants = false;

  @SuppressWarnings("unchecked")
  public synchronized void upgradeVariants(Map<String, List<ComponentType>> componentTypes) {
    if (upgradedVariants)
      return;

    upgradedVariants = true;

    LOG.info("Checking if variants need to be updated");
    Map<String, List<Template>> lookupMap =
        new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) configManager.readObject(IPlugInPort.TEMPLATES_KEY, null);

    if (variantMap == null)
      return;

    Map<String, ComponentType> typeMap =
        new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet())
      for (ComponentType type : entry.getValue()) {
        typeMap.put(type.getInstanceClass().getCanonicalName(), type);
        typeMap.put(type.getCategory() + "." + type.getName(), type);
        if (type.getCategory().contains("Electro-Mechanical"))
          typeMap.put(type.getCategory().replace("Electro-Mechanical", "Electromechanical") + "."
              + type.getName(), type);
      }

    Map<String, List<Template>> newVariantMap = new HashMap<String, List<Template>>();

    lookupMap.putAll(variantMap);

    for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
      if (typeMap.containsKey(entry.getKey())) {
        newVariantMap.put(typeMap.get(entry.getKey()).getInstanceClass().getCanonicalName(),
            entry.getValue()); // great,
                               // nothing
                               // to
                               // upgrade
      } else {
        LOG.warn("Could not upgrade variants for: " + entry.getKey());
      }
    }

    configManager.writeValue(IPlugInPort.TEMPLATES_KEY, newVariantMap);
  }

  @SuppressWarnings("unchecked")
  public List<Template> getVariantsFor(ComponentType type) {
    Map<String, List<Template>> lookupMap =
        new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);

    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) configManager.readObject(IPlugInPort.TEMPLATES_KEY, null);
    if (variantMap != null)
      lookupMap.putAll(variantMap);

    // try by class name and then by old category.type format
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();

    List<Template> variants = new ArrayList<Template>();
    if (variantMap != null) {
      List<Template> userVariants = variantMap.get(key1);
      if (userVariants != null && !userVariants.isEmpty())
        variants.addAll(userVariants);
      userVariants = variantMap.get(key2);
      if (userVariants != null && !userVariants.isEmpty())
        variants.addAll(userVariants);
    }
    Collections.sort(variants, new Comparator<Template>() {

      @Override
      public int compare(Template o1, Template o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return variants;
  }

  @SuppressWarnings("unchecked")
  public int importVariants(String fileName) throws IOException {
    LOG.debug(String.format("importVariants(%s)", fileName));
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));

    VariantPackage pkg = (VariantPackage) xStream.fromXML(in);

    in.close();

    if (pkg == null || pkg.getVariants().isEmpty())
      return 0;

    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) configManager.readObject(IPlugInPort.TEMPLATES_KEY, null);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<Template>>();
    }

    for (Map.Entry<String, List<Template>> entry : pkg.getVariants().entrySet()) {
      List<Template> templates;
      templates = variantMap.get(entry.getKey());
      if (templates == null) {
        templates = new ArrayList<Template>();
        variantMap.put(entry.getKey(), templates);
      }
      for (Template t : entry.getValue()) {
        templates.add(
            new Template(t.getName() + " [" + pkg.getOwner() + "]", t.getValues(), t.getPoints()));
      }
    }

    configManager.writeValue(IPlugInPort.TEMPLATES_KEY, variantMap);

    LOG.info(String.format("Loaded variants for %d components", pkg.getVariants().size()));

    return pkg.getVariants().size();
  }

  @SuppressWarnings("unchecked")
  public String getDefaultVariant(ComponentType type) {
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) configManager.readObject(IPlugInPort.DEFAULT_TEMPLATES_KEY, null);
    if (defaultTemplateMap == null)
      return null;

    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();

    if (defaultTemplateMap.containsKey(key1))
      return defaultTemplateMap.get(key1);

    return defaultTemplateMap.get(key2);
  }

  @SuppressWarnings("unchecked")
  public void deleteVariant(ComponentType type, String templateName) {
    LOG.debug(String.format("deleteTemplate(%s, %s)", type, templateName));
    Map<String, List<Template>> templateMap =
        (Map<String, List<Template>>) configManager.readObject(IPlugInPort.TEMPLATES_KEY, null);
    if (templateMap != null) {
      // try by class name and then by old category.type format
      String key1 = type.getInstanceClass().getCanonicalName();
      String key2 = type.getCategory() + "." + type.getName();

      List<Template> templates = templateMap.get(key1);
      if (templates != null) {
        Iterator<Template> i = templates.iterator();
        while (i.hasNext()) {
          Template t = i.next();
          if (t.getName().equalsIgnoreCase(templateName)) {
            i.remove();
          }
        }
      }
      templates = templateMap.get(key2);
      if (templates != null) {
        Iterator<Template> i = templates.iterator();
        while (i.hasNext()) {
          Template t = i.next();
          if (t.getName().equalsIgnoreCase(templateName)) {
            i.remove();
          }
        }
      }
    }
    configManager.writeValue(IPlugInPort.TEMPLATES_KEY, templateMap);
  }
}
