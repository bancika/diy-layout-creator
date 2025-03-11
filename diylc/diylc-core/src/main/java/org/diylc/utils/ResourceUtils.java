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
