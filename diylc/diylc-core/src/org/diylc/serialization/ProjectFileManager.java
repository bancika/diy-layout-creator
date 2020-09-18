/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.serialization;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.EventType;
import org.diylc.core.Project;
import org.diylc.presenter.Presenter;
import org.diylc.test.DIYTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class ProjectFileManager {

  private static final Logger LOG = Logger.getLogger(ProjectFileManager.class);

  private XStream xStream;
  // Legacy deserializer for 3.0.1 through 3.0.7, loads Points referenced in
  // pixels.
  private XStream xStreamOld;

  private String currentFileName = null;
  private boolean modified = false;

  private MessageDispatcher<EventType> messageDispatcher;

  private List<IOldFileParser> parsers;
  
  private Set<String> missingFields = new HashSet<String>();

  public ProjectFileManager(MessageDispatcher<EventType> messageDispatcher) {
    super();
    this.xStream = new XStream(new DomDriver("UTF-8")) {
      
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MapperWrapper(next) {
              @SuppressWarnings("rawtypes")
              @Override
              public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                  if (definedIn == Object.class) {
                      missingFields.add(definedIn.getName() + "." + fieldName);
                      return false;
                  }
                  return super.shouldSerializeMember(definedIn, fieldName);
              }
          };
      }
    };
    xStream.autodetectAnnotations(true);
    xStream.alias("point", java.awt.geom.Point2D.class);
    xStream.alias("point", java.awt.geom.Point2D.Double.class);
    xStream.alias("font", java.awt.Font.class);
    xStream.alias("project", Project.class);
    xStream.aliasPackage("diylc", "org.diylc.components");
    xStream.registerConverter(new PointConverter());        
    xStream.registerConverter(new ColorConverter());
    xStream.registerConverter(new FontConverter());
    xStream.registerConverter(new MeasureConverter());
    xStream.registerConverter(new AreaConverter());
    xStream.addImmutableType(Color.class);
    xStream.addImmutableType(java.awt.geom.Point2D.class);
    xStream.addImmutableType(java.awt.geom.Point2D.Double.class);
    xStream.addImmutableType(org.diylc.core.measures.Voltage.class);
    xStream.addImmutableType(org.diylc.core.measures.Resistance.class);
    xStream.addImmutableType(org.diylc.core.measures.Capacitance.class);
    xStream.addImmutableType(org.diylc.core.measures.Current.class);
    xStream.addImmutableType(org.diylc.core.measures.Power.class);
    xStream.addImmutableType(org.diylc.core.measures.Inductance.class);
    xStream.addImmutableType(org.diylc.core.measures.Size.class);
    this.xStreamOld = new XStream(new DomDriver());
    xStreamOld.autodetectAnnotations(true);
    this.messageDispatcher = messageDispatcher;
  }

  public void startNewFile() {
    currentFileName = null;
    modified = false;
    fireFileStatusChanged();
  }

  public List<IOldFileParser> getParsers() {
    if (parsers == null) {
      parsers = new ArrayList<IOldFileParser>();
      Set<Class<?>> componentTypeClasses = null;
      try {
        componentTypeClasses = Utils.getClasses("org.diylc.parsing");
        for (Class<?> clazz : componentTypeClasses) {
          if (!Modifier.isAbstract(clazz.getModifiers()) && IOldFileParser.class.isAssignableFrom(clazz)) {
            IOldFileParser instance = (IOldFileParser) clazz.newInstance();
            parsers.add(instance);
          }
        }
      } catch (Exception e) {
        LOG.error("Could not find old version parsers", e);
      }
    }
    return parsers;
  }

  public synchronized void serializeProjectToFile(Project project, String fileName, boolean isBackup)
      throws IOException {
    if (!isBackup) {
      LOG.info(String.format("serializeProjectToFile(%s)", fileName));
    }
    FileOutputStream fos;
    // write to a temp file to avoid loss of data in case of crash
    String tempName = fileName + ".temp";
    fos = new FileOutputStream(tempName);
    Writer writer = new OutputStreamWriter(fos, "UTF-8");
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    xStream.toXML(project, writer);
    fos.close();
    // switch the files at the end
    new File(fileName).delete();
    new File(tempName).renameTo(new File(fileName));
    if (!isBackup) {
      this.currentFileName = fileName;
      this.modified = false;
      fireFileStatusChanged();
    }
  }

  public Project deserializeProjectFromFile(String fileName, List<String> warnings) throws SAXException, IOException,
      ParserConfigurationException {
    LOG.info(String.format("loadProjectFromFile(%s)", fileName));
    Project project = null;
    File file = new File(fileName);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(new InputSource(new InputStreamReader(new FileInputStream(file))));
    doc.getDocumentElement().normalize();
    if (doc.getDocumentElement().getNodeName().equals(Project.class.getName()) || doc.getDocumentElement().getNodeName().equals("project")) {
      project = parseV3File(fileName, warnings);
    } else {
      if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase("layout")) {
        throw new IllegalArgumentException("Could not open DIY file. Root node is not named 'Layout'.");
      }
      String formatVersion = doc.getDocumentElement().getAttribute("formatVersion");

      // try to find a parser for an older version
      List<IOldFileParser> parsers = getParsers();
      for (int i = 0; i < parsers.size(); i++) {
        if (parsers.get(i).canParse(formatVersion))
          project = parsers.get(i).parseFile(doc.getDocumentElement(), warnings);
      }

      if (project == null)
        throw new IllegalArgumentException("Unknown file format version: " + formatVersion);
    }
    Collections.sort(warnings);
    this.currentFileName = fileName;
    this.modified = false;
    return project;
  }
  
  public void serializeTestToFile(DIYTest test, String fileName) throws IOException {
    LOG.info(String.format("serializeTestToFile(%s)", fileName));
    FileOutputStream fos;
    fos = new FileOutputStream(fileName);
    Writer writer = new OutputStreamWriter(fos, "UTF-8");
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    xStream.toXML(test, writer);
    fos.close();
  }

  public DIYTest deserializeTestFromFile(String fileName)
      throws SAXException, IOException, ParserConfigurationException {
    LOG.info(String.format("deserializeTestFromFile(%s)", fileName));
    DIYTest project = null;
    FileInputStream fis = new FileInputStream(fileName);
    Reader reader = new InputStreamReader(fis, "UTF-8");
    project = (DIYTest) xStream.fromXML(reader);
    fis.close();
    return project;
  }

  public void notifyFileChange() {
    this.modified = true;
    fireFileStatusChanged();
  }

  public String getCurrentFileName() {
    return currentFileName;
  }

  public boolean isModified() {
    return modified;
  }

  public void fireFileStatusChanged() {
    messageDispatcher.dispatchMessage(EventType.FILE_STATUS_CHANGED, getCurrentFileName(), isModified());
  }

  private Project parseV3File(String fileName, List<String> warnings) throws IOException {
    Project project;
    VersionNumber fileVersion;
    try { 
      fileVersion = readV3Version(fileName);
      if (fileVersion.compareTo(Presenter.CURRENT_VERSION) > 0)
        warnings.add("The file is created with a newer version of DIYLC and may contain features that are not supported by your version of DIYLC. Please update.");
    } catch (Exception e) {
      warnings.add("Could not read file version number, the file may be corrupted.");
    }
    FileInputStream fis = new FileInputStream(fileName);
    missingFields.clear();
    try {
      Reader reader = new InputStreamReader(fis, "UTF-8");
      project = (Project) xStream.fromXML(reader);
    } catch (Exception e) {
      LOG.warn("Could not open with the new xStream, trying the old one", e);
      fis.close();
      fis = new FileInputStream(fileName);
      project = (Project) xStreamOld.fromXML(fis);
    }
    if (!missingFields.isEmpty()) {
      LOG.warn("The opened file is missing the following properties: " + String.join(", ", missingFields));
      warnings.add("The project references unknown component properties, most likely because it was created with a newer version of DIYLC.");
    }
    fis.close();
    return project;
  }
  
  private VersionNumber readV3Version(String fileName) throws Exception {
    File fXmlFile = new File(fileName);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(fXmlFile);
            
    doc.getDocumentElement().normalize();
    
    NodeList nList = doc.getElementsByTagName("fileVersion");
    
    int items = nList.getLength();
    
    if (items != 1)
      throw new Exception("File version information could not be read from the XML.");
    
    Node versionNode = nList.item(0);    
    Node n = versionNode.getFirstChild().getNextSibling();
    int major = Integer.parseInt(n.getFirstChild().getNodeValue());
    n = n.getNextSibling().getNextSibling();
    int minor = Integer.parseInt(n.getFirstChild().getNodeValue());
    n = n.getNextSibling().getNextSibling();
    int build = Integer.parseInt(n.getFirstChild().getNodeValue());
    
    return new VersionNumber(major, minor, build);

  }
}
