package org.diylc.swing.plugins.online.presenter;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

public class LibraryPresenter {

	private final static Logger LOG = Logger.getLogger(LibraryPresenter.class);

	private IServiceAPI service;
	private String serviceUrl;

	public LibraryPresenter() {
		serviceUrl = ConfigurationManager.getInstance().readString(
				IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api");
		ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
		service = factory.createProxy(IServiceAPI.class, serviceUrl);
	}

}
