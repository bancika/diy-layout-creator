package org.diylc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.log4j.BasicConfigurator;
import org.diylc.swing.plugins.online.model.ProjectEntity;

import com.diyfever.gui.objecttable.IActionProcessor;
import com.diyfever.gui.objecttable.ObjectListTable;

public class TableTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();

		List<ProjectEntity> projects = new ArrayList<ProjectEntity>();
		projects.add(new ProjectEntity(1, "first", "some text", "bane", "none"));
		projects.add(new ProjectEntity(2, "second", "anoter text", "bisera", "none"));

		ObjectListTable<ProjectEntity> t = new ObjectListTable<ProjectEntity>(ProjectEntity.class,
				new String[] { "getName", "getDescription", "getCategory", "getOwner",
						"action:Download" }, new IActionProcessor<ProjectEntity>() {

					@Override
					public void actionExecuted(ProjectEntity value, String actionColumnName) {
						System.out.println("row clicked: " + actionColumnName + " - " + value);
					}

					@Override
					public Icon getActionIcon(String actionColumnName) {
						return null;
					}

					@Override
					public String getActionLabel(String actionColumnName) {
						return "get me";
					}
				});
		t.setData(projects);
		JFrame f = new JFrame();
		f.add(new JScrollPane(t));
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
