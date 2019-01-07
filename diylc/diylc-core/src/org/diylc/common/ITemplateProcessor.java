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
package org.diylc.common;

import java.util.List;

import org.diylc.core.Template;

public interface ITemplateProcessor {

  public static final String TEMPLATES_KEY = "templates";
  public static final String DEFAULT_TEMPLATES_KEY = "defaultTemplates";

  void saveSelectedComponentAsTemplate(String templateName);

  List<Template> getTemplatesFor(String categoryName, String componentTypeName);

  List<Template> getTemplatesForSelection();

  void applyTemplateToSelection(Template template);

  void deleteTemplate(String categoryName, String componentTypeName, String templateName);

  void setTemplateDefault(String categoryName, String componentTypeName, String templateName);
  
  String getDefaultTemplate(String categoryName, String componentTypeName);

  public class TemplateAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}
