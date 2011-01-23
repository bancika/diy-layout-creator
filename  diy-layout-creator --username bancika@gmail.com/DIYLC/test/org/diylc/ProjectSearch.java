package org.diylc;

import java.util.List;

import org.diylc.swing.plugins.online.model.ProjectEntity;

import com.diyfever.httpproxy.ParamName;

public interface ProjectSearch {
	List<ProjectEntity> project_search(@ParamName("criteria") String criteria);
}