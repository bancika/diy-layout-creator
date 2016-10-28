package org.diylc.presenter;

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
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.EventType;
import org.diylc.core.Project;
import org.diylc.parsing.IOldFileParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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

  public ProjectFileManager(MessageDispatcher<EventType> messageDispatcher) {
    super();
    this.xStream = new XStream(new DomDriver("UTF-8"));
    xStream.autodetectAnnotations(true);
    xStream.registerConverter(new PointConverter());
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
      LOG.info(String.format("saveProjectToFile(%s)", fileName));
    }
    FileOutputStream fos;
    fos = new FileOutputStream(fileName);
    Writer writer = new OutputStreamWriter(fos, "UTF-8");
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    xStream.toXML(project, writer);
    fos.close();
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
    if (doc.getDocumentElement().getNodeName().equalsIgnoreCase(Project.class.getName())) {
      project = parseV3File(fileName);
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

  private Project parseV3File(String fileName) throws IOException {
    Project project;
    FileInputStream fis = new FileInputStream(fileName);
    try {
      Reader reader = new InputStreamReader(fis, "UTF-8");
      project = (Project) xStream.fromXML(reader);
    } catch (Exception e) {
      LOG.warn("Could not open with the new xStream, trying the old one");
      fis.close();
      fis = new FileInputStream(fileName);
      project = (Project) xStreamOld.fromXML(fis);
    }
    fis.close();
    return project;
  }
}
