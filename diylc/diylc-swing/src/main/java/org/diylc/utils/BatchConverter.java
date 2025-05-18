/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;

import org.diylc.DIYLCStarter;
import org.diylc.presenter.Presenter;
import org.diylc.common.DummyView;

public class BatchConverter {
  
  private static final Logger LOG = Logger.getLogger(BatchConverter.class);

  public static void main(String[] args) {
    URL url = DIYLCStarter.class.getResource("/log4j.properties");
    Properties properties = new Properties();
    try (InputStream inputStream = url.openStream()) {
      properties.load(inputStream);
      PropertyConfigurator.configure(properties);
    } catch (Exception e) {
      LOG.error("Could not initialize log4j configuration", e);
    }

    File dir = new File("C:\\Users\\Branislav Stojkovic\\Documents\\layouts_v1");
    File[] matchingFiles = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return true;
      }
    });
    Presenter presenter = new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance());
    for (File file : matchingFiles) {
      if (file.getName() != "" && file.getName() != "." && file.getName() != ".." && !file.isDirectory())
      presenter.loadProjectFromFile(file.getAbsolutePath());
      presenter.saveProjectToFile(file.getParentFile().getAbsolutePath() + "\\converted\\" + file.getName(), false);
    }
  }

}
