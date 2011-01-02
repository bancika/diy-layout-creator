package com.diyfever.diylc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import com.diyfever.diylc.plugins.online.model.ProjectEntity;
import com.diyfever.httpproxy.ParamName;
import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// XStream xstream = new XStream(new JettisonMappedXmlDriver());
		// xstream.setMode(XStream.NO_REFERENCES);
		//
		// List<ProjectEntity> projects = new ArrayList<ProjectEntity>();
		// // projects.add(new ProjectEntity(1, "first project", "some text",
		// // "bancika", "some category"));
		// // projects.add(new ProjectEntity(2, "second project", "some text",
		// // "bancika", "some category"));
		// System.out.println(xstream.toXML(projects));

		BasicConfigurator.configure();
		ProjectSearch search = new ProxyFactory(new PhpFlatProxy()).createProxy(
				ProjectSearch.class, "http://host07.dwhost.net/~diyfever/diylc/db/");
		List<ProjectEntity> results = search.project_search("TestProject");
		System.out.println(results);
	}

}
