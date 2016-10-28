package org.diylc.parsing;

import java.util.List;

import org.diylc.core.Project;
import org.w3c.dom.Element;

public interface IOldFileParser {

  boolean canParse(String version);

  Project parseFile(Element root, List<String> warnings);
}
