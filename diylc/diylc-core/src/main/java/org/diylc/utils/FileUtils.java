package org.diylc.utils;

import java.io.File;

public class FileUtils {

  /**
   * Extracts the base file name without extension from a full path.
   * For example: "/path/to/myfile.diy" returns "myfile"
   */
  public static String extractFileName(String path) {
    if (path == null || path.isEmpty()) {
      return "Untitled";
    }
    // Get the last part after the last separator
    String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
    // Remove the extension if present
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      fileName = fileName.substring(0, dotIndex);
    }
    return fileName;
  }
}
