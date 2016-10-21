package org.diylc.swing.plugins.online.presenter;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.swing.plugins.online.model.ServiceAPI;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

public class OnlinePresenter {

	private static String USERNAME_KEY = "cloud.Username";
	private static String TOKEN_KEY = "cloud.token";

	private final static Logger LOG = Logger.getLogger(OnlinePresenter.class);

	private ServiceAPI service;
	private String serviceUrl;
	private String machineId;

	private CloudListener listener;

	private boolean loggedIn = false;

	public OnlinePresenter(CloudListener listener) {
		serviceUrl = ConfigurationManager.getInstance().readString(
				ServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api");
		ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
		service = factory.createProxy(ServiceAPI.class, serviceUrl);
		if (service == null)
			LOG.warn("Service proxy not created!");
		this.listener = listener;
	}

	public boolean logIn(String username, String password) {
		if (service == null)
			return false;
		LOG.info("Trying to log in to cloud as " + username);
		String res = service.login(username, password, getMachineId());
		if (res.equals("Error")) {
			LOG.info("Log in failed");
			return false;
		} else {
			LOG.info("Log in success");
			ConfigurationManager.getInstance().writeValue(USERNAME_KEY,
					username);
			ConfigurationManager.getInstance().writeValue(TOKEN_KEY, res);
			listener.loggedIn();
			this.loggedIn = true;
			return true;
		}
	}

	public boolean tryLogInWithToken() {
		if (service == null)
			return false;

		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);
		String token = ConfigurationManager.getInstance().readString(TOKEN_KEY,
				null);

		if (username != null && token != null) {
			LOG.info("Trying to log in to cloud using a token as " + username);
			String res = service
					.loginWithToken(username, token, getMachineId());
			if (res.equals("Error")) {
				LOG.info("Log in failed");
				return false;
			} else {
				LOG.info("Log in success");
				listener.loggedIn();
				this.loggedIn = true;
				return true;
			}
		} else
			return false;
	}

	public void logOut() {
		LOG.info("Logged out");
		ConfigurationManager.getInstance().writeValue(TOKEN_KEY, null);
		this.loggedIn = false;
		listener.loggedOut();
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	private String getMachineId() {
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
			LOG.info("Local machine id: " + machineId);
		}
		return machineId;
	}
}
