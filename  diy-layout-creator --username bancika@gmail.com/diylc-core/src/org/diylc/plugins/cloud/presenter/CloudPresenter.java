package org.diylc.plugins.cloud.presenter;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.model.ServiceAPI;
import org.diylc.plugins.cloud.model.UserEntity;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

public class CloudPresenter {

	private static String USERNAME_KEY = "cloud.Username";
	private static String TOKEN_KEY = "cloud.token";

	private static String ERROR = "Error";

	private final static Logger LOG = Logger.getLogger(CloudPresenter.class);
	private static final Object SUCCESS = "Success";

	private ServiceAPI service;
	private String serviceUrl;
	private String machineId;
	private String[] categories;

	private CloudListener listener;

	private boolean loggedIn = false;

	public CloudPresenter(CloudListener listener) {
		serviceUrl = ConfigurationManager.getInstance().readString(
				ServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1");
		ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
		service = factory.createProxy(ServiceAPI.class, serviceUrl);
		if (service == null)
			LOG.warn("Service proxy not created!");
		this.listener = listener;
	}

	public boolean logIn(String username, String password)
			throws CloudException {
		if (service == null)
			return false;
		LOG.info("Trying to login to cloud as " + username);

		String res;
		try {
			res = service.login(username, password, getMachineId());
		} catch (Exception e) {
			throw new CloudException(e);
		}

		if (res == null || res.equals(ERROR)) {
			LOG.info("Login failed");
			return false;
		} else {
			LOG.info("Login success");
			ConfigurationManager.getInstance().writeValue(USERNAME_KEY,
					username);
			ConfigurationManager.getInstance().writeValue(TOKEN_KEY, res);
			listener.loggedIn();
			this.loggedIn = true;
			return true;
		}
	}

	public boolean tryLogInWithToken() throws CloudException {
		if (service == null)
			return false;

		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);
		String token = ConfigurationManager.getInstance().readString(TOKEN_KEY,
				null);

		if (username != null && token != null) {
			LOG.info("Trying to login to cloud using a token as " + username);
			String res;
			try {
				res = service.loginWithToken(username, token, getMachineId());
			} catch (Exception e) {
				throw new CloudException(e);
			}
			if (res == null || res.equals(ERROR)) {
				LOG.info("Login failed");
				return false;
			} else {
				LOG.info("Login success");
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

	public String[] getCategories() throws CloudException {
		if (categories == null) {
			Object res;
			try {
				res = service.getCategories();
				if (res != null && res.equals(ERROR))
					throw new CloudException(
							"Could not fetch categories from the server.");
				if (res instanceof List<?>) {
					List<String> cats = (List<String>) res;
					cats.add(0, "");
					categories = cats.toArray(new String[0]);
				} else {
					throw new CloudException(
							"Unexpected server response received for category list: "
									+ res.getClass().getName());
				}
			} catch (Exception e) {
				throw new CloudException(e);
			}
		}
		return categories;
	}

	public void upload(String projectName, String category, String description,
			String keywords, String diylcVersion, File thumbnail, File project)
			throws IOException, CloudException {
		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);
		String token = ConfigurationManager.getInstance().readString(TOKEN_KEY,
				null);

		if (username == null || token == null)
			throw new CloudException("Login failed. Please try to login again.");

		try {
			String res = service.upload(username, token, getMachineId(),
					projectName, category, description, diylcVersion, keywords,
					thumbnail, project);
			if (!res.equals(SUCCESS))
				throw new CloudException(res);
		} catch (Exception e) {
			throw new CloudException(e);
		}

	}

	public void createUserAccount(String username, String password,
			String email, String website, String bio) throws CloudException {
		try {
			String res = service.createUser(username, password, email, website,
					bio);
			if (res == null)
				throw new CloudException("Could not create user account.");
			if (!SUCCESS.equals(res))
				throw new CloudException(res);
		} catch (Exception e) {
			throw new CloudException(e);
		}
	}

	public UserEntity getUserDetails() throws CloudException {
		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);
		String token = ConfigurationManager.getInstance().readString(TOKEN_KEY,
				null);

		Object res;
		try {
			res = service.getUserDetails(username, token, getMachineId());
		} catch (Exception e) {
			throw new CloudException(e);
		}
		if (res instanceof String)
			throw new CloudException(res.toString());
		if (res instanceof UserEntity)
			return (UserEntity) res;
		throw new CloudException(
				"Unexpected server response received for category list: "
						+ res.getClass().getName());
	}

	public void updatePassword(String oldPassword, String newPassword)
			throws CloudException {
		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);

		String res;
		try {
			res = service.updatePassword(username, oldPassword, newPassword);
		} catch (Exception e) {
			throw new CloudException(e);
		}
		if (!res.equals(SUCCESS))
			throw new CloudException(res);
	}

	public void updateUserDetails(String email, String website, String bio)
			throws CloudException {
		String username = ConfigurationManager.getInstance().readString(
				USERNAME_KEY, null);
		String token = ConfigurationManager.getInstance().readString(TOKEN_KEY,
				null);

		String res;
		try {
			res = service.updateUserDetails(username, token, getMachineId(),
					email, website, bio);
		} catch (Exception e) {
			throw new CloudException(e);
		}
		if (!res.equals(SUCCESS))
			throw new CloudException(res);
	}

	public List<ProjectEntity> search(String criteria, String category,
			String sortOrder, int pageNumber, int itemsPerPage)
			throws CloudException {
		try {
			Object res = service.search(criteria, category, "json", pageNumber,
					itemsPerPage, sortOrder);
			if (res == null)
				throw new CloudException("Failed to retreive search results.");
			if (res instanceof String)
				throw new CloudException(res.toString());
			if (res instanceof List<?>)
				return (List<ProjectEntity>) res;
			throw new CloudException(
					"Unexpected server response received for search results: "
							+ res.getClass().getName());
		} catch (Exception e) {
			throw new CloudException(e);
		}
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
