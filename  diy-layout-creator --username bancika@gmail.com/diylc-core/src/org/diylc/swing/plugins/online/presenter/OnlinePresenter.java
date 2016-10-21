package org.diylc.swing.plugins.online.presenter;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.swing.plugins.online.model.ServiceAPI;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

public class OnlinePresenter {

	private final static Logger LOG = Logger.getLogger(OnlinePresenter.class);

	private ServiceAPI service;
	private String serviceUrl;
	private String machineId;

	public OnlinePresenter() {
		serviceUrl = ConfigurationManager.getInstance().readString(
				ServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api");
		ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
		service = factory.createProxy(ServiceAPI.class, serviceUrl);
	}
	
	public boolean login(String username, String password) {
		String res = service.login(username, password, getMachineId());
		if (res.equals("Error"))
		{
			return false;
		}
		else {
			return true;
		}
	}

	public String getMachineId() {
		if (machineId == null) {
			try {
				InetAddress ip = InetAddress.getLocalHost();

				NetworkInterface network = NetworkInterface
						.getByInetAddress(ip);

				byte[] mac = network.getHardwareAddress();

				StringBuilder sb = new StringBuilder(18);
				for (byte b : mac) {
					if (sb.length() > 0)
						sb.append(':');
					sb.append(String.format("%02x", b));
				}

				machineId = sb.toString();
			} catch (Exception e) {
				machineId = "Generic";
			}
		}
		return machineId;
	}
}
