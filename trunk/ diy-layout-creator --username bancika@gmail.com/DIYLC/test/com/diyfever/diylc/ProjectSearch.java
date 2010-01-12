package com.diyfever.diylc;

import java.util.List;

import com.diyfever.diylc.plugins.online.model.ProjectEntity;
import com.diyfever.httpproxy.ParamName;

public interface ProjectSearch {
	List<ProjectEntity> project_search(@ParamName("criteria") String criteria);
}