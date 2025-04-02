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
package org.diylc.utils;

import org.apache.log4j.Logger;
import org.diylc.lang.LangUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtils {

    private static final Logger LOG = Logger.getLogger(ResourceUtils.class);

    public static Map<String, String> getResourceContents(String dir) {
        Map<String, String> contents = new HashMap<>();
        
        try {
            // Get all resources in the directory
            URL dirUrl = ResourceUtils.class.getClassLoader().getResource(dir);
            if (dirUrl != null) {
                if (dirUrl.getProtocol().equals("jar")) {
                    // Running from JAR
                    String jarPath = dirUrl.getPath();
                    // Remove the "file:" prefix and decode the URL
                    jarPath = URLDecoder.decode(jarPath.substring(5, jarPath.indexOf("!")), StandardCharsets.UTF_8);
                    JarFile jarFile = new JarFile(jarPath);
                    try {
                        jarFile.stream()
                            .filter(entry -> entry.getName().startsWith(dir + "/") && !entry.isDirectory())
                            .forEach(entry -> {
                                try {
                                    String relativePath = entry.getName().substring(dir.length() + 1);
                                    // Read the entire file content as string
                                    String content = new String(jarFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                                    contents.put(relativePath, content);
                                } catch (IOException e) {
                                    LOG.warn("Could not read resource: " + entry.getName(), e);
                                }
                            });
                    } finally {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            LOG.warn("Could not close JAR file", e);
                        }
                    }
                } else {
                    // Running from filesystem
                    java.io.File dirFile = new java.io.File(dirUrl.toURI());
                    if (dirFile.exists() && dirFile.isDirectory()) {
                        java.io.File[] files = dirFile.listFiles();
                        if (files != null) {
                            for (java.io.File file : files) {
                                if (file.isFile()) {
                                    try {
                                        String relativePath = file.getName();
                                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                                        contents.put(relativePath, content);
                                    } catch (IOException e) {
                                        LOG.warn("Could not read file: " + file, e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error loading resources from directory: " + dir, e);
        }
        
        return contents;
    }
}
