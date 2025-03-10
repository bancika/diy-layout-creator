/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.utils.ResourceLoader;

public class LangUtil {

  private static final String LANG_DIR = "lang";

  private static final Logger LOG = Logger.getLogger(LangUtil.class);

  private static Map<String, String> dict = null;
  private static List<String> missing;

  public static String translate(String text) {
    if (text == null || text.length() <= 1)
      return text;

    try {
      // don't translate numbers
      Double.parseDouble(text);
      return text;
    } catch (Exception e) {
    }
    
    if (dict != null) {
      if (dict.containsKey(text.toLowerCase())) {
        return dict.get(text.toLowerCase());
      } else {
        if ("true".equalsIgnoreCase(System.getProperty("org.diylc.storeDict"))) {
          List<String> missing = getMissing();
          if (!missing.contains(text)) {
            missing.add(text);
            Collections.sort(missing);
            try (PrintWriter out = new PrintWriter("dict.txt")) {
              for (String m : missing) {
                out.println(m);
              }
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    return text;
  }

  public static void configure() {    
    String language = ConfigurationManager.getInstance().readString(IPlugInPort.LANGUAGE, IPlugInPort.LANGUAGE_DEFAULT);
    if (language != null)
      load(language);
  }

  private static void load(String language) {
    LOG.info("Loading translation for " + language);
    dict = new HashMap<String, String>();
    try {
      String path = ResourceLoader.getFile(LANG_DIR + File.separator + language + ".txt").getAbsolutePath();
      LOG.debug("Loading language file: " + path);
      try (Stream<String> stream = Files.lines(
          Paths.get(path), StandardCharsets.UTF_8)) {
        stream.forEach(s -> {
          if (s.contains("|")) {
            String[] parts = s.split("\\|");
            if (parts.length == 2)
              dict.put(parts[0].toLowerCase().trim(), parts[1].trim());
          } else {
            dict.put(s.toLowerCase(), s);
          }
        });
      } catch (IOException e) {
        LOG.error("Error loading language file", e);
      }
    } catch (Exception e) {
      LOG.error("Error loading language file", e);
    }
  }

  private static List<String> getMissing() {
    if (missing == null) {
      missing = new ArrayList<String>();
      try {
        try (Stream<String> stream = Files.lines(Paths.get("dict.txt"), StandardCharsets.UTF_8)) {
          stream.forEach(s -> missing.add(s));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {

      }
    }
    return missing;
  }

  public static List<Language> getAvailableLanguages() {
    List<Language> res = new ArrayList<Language>();
    File[] files = ResourceLoader.getFiles(LANG_DIR);
    if (files == null)
      return res;
    try {
      for (File file : files) {        
        String key = file.getName().replaceFirst("[.][^.]+$", "");
        Map<String, String> details = new HashMap<String, String>();
        try (Stream<String> stream = Files.lines(
            Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
          stream.forEach(s -> {
            if (s.contains("=")) {
              String[] parts = s.split("=");
              if (parts.length == 2)
                details.put(parts[0].toLowerCase().trim(), parts[1].trim());
            }
          });
        } catch (IOException e) {
          LOG.error("Error loading language file", e);
        }
        res.add(new Language(key, details));
      }
      Collections.sort(res);
      return res;
    } catch (Exception e) {
      LOG.error("Error fetching available languages", e);
      return null;
    }
  }
}
