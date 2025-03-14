package org.diylc.presenter;

import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.thoughtworks.xstream.XStream;
import org.diylc.core.Project;
import org.diylc.test.DIYTest;

public interface ProjectFileService {
    // Project operations
    void serializeProjectToFile(Project project, String fileName, boolean isBackup) throws IOException;
    Project deserializeProjectFromFile(String fileName, List<String> warnings) 
            throws SAXException, IOException, ParserConfigurationException;
    
    // Test operations
    void serializeTestToFile(DIYTest test, String fileName) throws IOException;
    DIYTest deserializeTestFromFile(String fileName) 
            throws SAXException, IOException, ParserConfigurationException;
    
    // File status
    void startNewFile();
    void notifyFileChange();
    String getCurrentFileName();
    boolean isModified();
    void fireFileStatusChanged();
    
    // Serialization
    XStream getXStream();
} 