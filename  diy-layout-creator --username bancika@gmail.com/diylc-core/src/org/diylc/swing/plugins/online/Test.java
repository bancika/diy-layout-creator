package org.diylc.swing.plugins.online;

import org.apache.log4j.BasicConfigurator;
import org.diylc.swing.plugins.online.presenter.LibraryPresenter;


public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		LibraryPresenter pres = new LibraryPresenter();
		// pres.createUser("bancika", "pwd", "bancika@gmail.com");
		System.out.println(pres.fetchCategories());
		pres.login("bancika", "pwd");
		int id = pres.fetchMyProjectRows().get(0).getId();
		// InputStream stream = pres.downloadProjectContent(id);
		// byte[] buf = new byte[100];
		// int len = stream.read(buf);
		System.out.println(pres.downloadProjectContent(id));
		// System.out.println(pres.hashPassword("bancikabancikabancikabancikabancika"));
		pres.dispose();
	}

}
