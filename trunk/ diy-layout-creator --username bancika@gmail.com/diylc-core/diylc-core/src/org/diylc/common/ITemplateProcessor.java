package org.diylc.common;

import java.util.List;

import org.diylc.core.Template;

public interface ITemplateProcessor {

	public static final String TEMPLATES_KEY = "templates";

	void saveSelectedComponentAsTemplate(String templateName);

	List<Template> getTemplatesFor(String categoryName, String componentTypeName);
	
	List<Template> getTemplatesForSelection();
	
	void applyTemplateToSelection(Template template);
	
	void deleteTemplate(String categoryName, String componentTypeName, String templateName);

	public class TemplateAlreadyExistsException extends Exception {

		private static final long serialVersionUID = 1L;

	}
}
