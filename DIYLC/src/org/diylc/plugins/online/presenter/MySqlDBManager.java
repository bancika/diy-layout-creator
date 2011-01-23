package org.diylc.plugins.online.presenter;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class MySqlDBManager {

	private final static Logger LOG = Logger.getLogger(MySqlDBManager.class);

	protected Connection connection;
	protected Statement statement;
	protected ResultSet resultSet;
	protected String url;
	protected String user;
	protected String pass;
	protected boolean connected;

	public MySqlDBManager(String url, String user, String pass) {
		this.url = url;
		this.user = user;
		this.pass = pass;
		this.connected = false;
	}

	public MySqlDBManager(String host, String port, String userName, String password) {
		this(String.format("jdbc:mysql://%s%s", host, port.trim().equals("") ? "" : (":" + port)),
				userName, password);
	}

	public void connectDB() throws Exception {
		if (!connected) {
			Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.registerDriver(driver);
			String finalUrl = String.format("%s?user=%s&password=%s", url, user, pass);
			LOG.info("Trying to connect to: " + url);
			connection = DriverManager.getConnection(finalUrl);
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			connected = true;
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void closeConnection() {
		LOG.info("Closing connection.");
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
			connected = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet getResultSet(String query) throws SQLException {
		LOG.info("Executing query: " + query);
		resultSet = statement.executeQuery(query);
		return resultSet;
	}

	public Connection getConnection() {
		return connection;
	}

	public int updateDB(String query) throws SQLException {
		LOG.info("Executing query: " + query);
		statement.execute(query);
		return statement.getUpdateCount();
	}
}