package org.diylc.utils;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.List;

import org.diylc.appframework.update.Change;
import org.diylc.appframework.update.Version;
import org.diylc.presenter.Presenter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class VersionReader {
  public static void main(String[] args) {
    try {
      URL resource = Presenter.class.getResource("update.xml");
      if (resource != null) {
        BufferedInputStream in = new BufferedInputStream(resource.openStream());
        XStream xStream = new XStream(new DomDriver());
        @SuppressWarnings("unchecked")
        List<Version> allVersions = (List<Version>) xStream.fromXML(in);
        Version latest = allVersions.get(allVersions.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (Change c : latest.getChanges())
          sb.append("**[" + c.getChangeType() + "]** " + c.getDescription()).append("\n");
        System.out.println(sb.toString());
        in.close();
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
