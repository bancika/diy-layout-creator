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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {

    private static final Logger LOG = Logger.getLogger(LangUtil.class);

    public static List<File> getResourceFiles(String dir) {
        List<File> files = new ArrayList<>();
        URL url = LangUtil.class.getResource("/" + dir);
        Path path = null;
        try {
            path = Paths.get(url.toURI());
            Files.walk(path, 5).forEach(p -> {
                if (!p.toFile().isDirectory()) {
                    files.add(p.toFile());
                }
            });
        } catch (URISyntaxException e) {
            LOG.error("Error loading language file", e);
        } catch (IOException e) {
            LOG.error("Error loading language file", e);
        }
        return files;
    }
}
