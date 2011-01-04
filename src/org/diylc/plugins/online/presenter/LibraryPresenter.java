package org.diylc.plugins.online.presenter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.plugins.online.model.ProjectEntity;


public class LibraryPresenter {

	private final static Logger LOG = Logger.getLogger(LibraryPresenter.class);

	private static final String DB_HOST = "diy-fever.com";
	private static final String DB_PORT = "3306";
	private static final String DB_USER = "diylc-app";
	private static final String DB_PASS = "zuf7RuS8";

	private static final String CATEGORY_SQL = "SELECT * FROM diylc.category WHERE 1=1";
	private static final String USER_SQL = "SELECT * FROM diylc.user WHERE name = \"%s\"";
	private static final String CREATE_USER_SQL = "INSERT INTO diylc.user (name, password, email) VALUES (\"%s\", \"%s\", \"%s\")";
	private static final String MY_PROJECTS_SQL = "SELECT project_id, name, category_id, description FROM diylc.project WHERE owner_user_id = %s";
	private static final String PROJECT_SQL = "SELECT content FROM diylc.project WHERE project_id = %s";
	private static final String UPLOAD_PROJECT_SQL = "INSERT INTO diylc.project (name, description, content, category_id, owner_user_id) VALUES (\"%s\", \"%s\", \"%s\", %s, %s)";
	private static final String UPDATE_PROJECT_SQL = "UPDATE diylc.project SET name = \"%s\", description = \"%s\", content = \"%s\", category_id = %s WHERE project_id = %s";

	private MySqlDBManager dbManager;
	private MessageDigest messageDigest;
	private Integer currentUserId;

	public LibraryPresenter() {
		dbManager = new MySqlDBManager(DB_HOST, DB_PORT, DB_USER, DB_PASS);
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Could not initialize MD5, algorithm not found.");
		}
	}

	public boolean connectDb() {
		try {
			dbManager.connectDB();
			return true;
		} catch (Exception e) {
			LOG.error("Could not connect to the database: " + e.getMessage());
			return false;
		}
	}

	public void createUser(String userName, String password, String email) throws SQLException,
			NotConnectedException, AlreadyExistsException {
		LOG.info("Creating user: " + userName);
		if (dbManager.isConnected()) {
			ResultSet resultSet = dbManager.getResultSet(String.format(USER_SQL, userName));
			if (resultSet.next()) {
				throw new AlreadyExistsException();
			}
			dbManager.updateDB(String.format(CREATE_USER_SQL, userName, hashPassword(password),
					email));
			resultSet = dbManager.getResultSet(String.format(USER_SQL, userName));
			if (resultSet.next()) {
				LOG.info("Logged in as \"" + userName + "\"");
				currentUserId = resultSet.getInt("user_id");
			}
		} else {
			LOG.error("Not connected");
			throw new NotConnectedException();
		}
	}

	public boolean login(String userName, String password) throws SQLException,
			NotConnectedException {
		LOG.info("Logging in as: " + userName);
		if (dbManager.isConnected()) {
			ResultSet resultSet = dbManager.getResultSet(String.format(USER_SQL, userName));
			if (resultSet.next()) {
				String dbPassword = resultSet.getString("password");
				String hashPassword = hashPassword(password);
				if (dbPassword.equals(hashPassword)) {
					LOG.info("Logged in as \"" + userName + "\"");
					currentUserId = resultSet.getInt("user_id");
				} else {
					LOG.info("User name and password do not match.");
				}
			}
			return false;
		} else {
			LOG.error("Not connected");
			throw new NotConnectedException();
		}
	}

	public Map<Integer, String> fetchCategories() throws SQLException, NotConnectedException {
		LOG.info("Fetching categories");
		if (dbManager.isConnected()) {
			Map<Integer, String> categories = new HashMap<Integer, String>();
			ResultSet resultSet = dbManager.getResultSet(CATEGORY_SQL);
			while (resultSet.next()) {
				categories.put(resultSet.getInt("category_id"), resultSet.getString("name"));
			}
			return categories;
		} else {
			LOG.error("Not connected");
			throw new NotConnectedException();
		}
	}

	public List<ProjectEntity> fetchMyProjectRows() throws NotLoggedInException,
			NotConnectedException, SQLException {
		if (dbManager.isConnected()) {
			if (isLoggedIn()) {
				Map<Integer, String> categories = fetchCategories();
				List<ProjectEntity> projects = new ArrayList<ProjectEntity>();
				ResultSet resultSet = dbManager.getResultSet(String.format(MY_PROJECTS_SQL,
						currentUserId));
				while (resultSet.next()) {
					projects.add(new ProjectEntity(resultSet.getInt("project_id"), resultSet
							.getString("name"), resultSet.getString("description"), "", categories
							.get(resultSet.getInt("category_id"))));
				}
				return projects;
			} else {
				LOG.error("Not logged in");
				throw new NotLoggedInException();
			}
		} else {
			LOG.error("Not connected");
			throw new NotConnectedException();
		}
	}

	public String downloadProjectContent(int projectId) throws NotConnectedException,
			NotExistsException, SQLException {
		if (dbManager.isConnected()) {
			ResultSet resultSet = dbManager.getResultSet(String.format(PROJECT_SQL, projectId));
			if (resultSet.next()) {
				return resultSet.getString("content");
			}
			throw new NotExistsException("Project with id = " + projectId + " does not exist.");
		} else {
			LOG.error("Not connected");
			throw new NotConnectedException();
		}
	}

	public List<ProjectEntity> fetchProjectRows(String searchCriteria) {
		return null;
	}

	public boolean isLoggedIn() {
		return currentUserId != null;
	}

	public String hashPassword(String password) {
		messageDigest.reset();
		byte[] bytes = messageDigest.digest(password.getBytes());
		String md5 = "";
		for (byte b : bytes) {
			String str = Integer.toHexString(b);
			if (str.length() == 1) {
				str = "0" + str;
			}
			md5 += str.substring(str.length() - 2);
		}
		return md5;
	}

	public void dispose() {
		dbManager.closeConnection();
	}
}
