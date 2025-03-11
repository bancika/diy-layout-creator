package org.diylc.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.diylc.appframework.update.Change;
import org.diylc.appframework.update.Version;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import org.diylc.presenter.Presenter;

public class VersionReader {
  public static void main(String[] args) {
    try {
      InputStream inputStream = Presenter.class.getResourceAsStream("/update.xml");
      if (inputStream != null) {
        BufferedInputStream in = new BufferedInputStream(inputStream);
        XStream xStream = new XStream(new DomDriver());
        xStream.addPermission(AnyTypePermission.ANY);
        @SuppressWarnings("unchecked")
        List<Version> allVersions = (List<Version>) xStream.fromXML(in);
        Version latest = allVersions.get(allVersions.size() - 1);
        StringBuilder sbMd = new StringBuilder();
        StringBuilder sbHtml = new StringBuilder();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
        sbHtml.append("    <release version=\"").append(latest.getVersionNumber()).append("\" date=\"").append(dateFormat.format(latest.getReleaseDate())).append("\">\n");
        sbHtml.append("        <description>\n");
        sbHtml.append("          <ul>\n");        
          
        
        for (Change c : latest.getChanges()) {
          sbMd.append("**[" + c.getChangeType() + "]** " + c.getDescription()).append("\n");
          sbHtml.append("            <li>[" + c.getChangeType() + "] " + c.getDescription()).append("</li>\n");
        }
        
        sbHtml.append("          </ul>\n");
        sbHtml.append("        </description>\n");
        sbHtml.append("        <url>https://github.com/bancika/diy-layout-creator/releases/tag/v").append(latest.getVersionNumber()).append("</url>\n");
        sbHtml.append("    </release>\n");
        
        System.out.println(sbMd.toString());
        System.out.println();
        System.out.println(sbHtml.toString());
        in.close();
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
