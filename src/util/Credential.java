package util;

public class Credential {
	
	public String username;
	
	public String password;
	
	public String server;
	
	public String database;

	public Credential(String username, String password, String server, String database) {
		this.username = username;
		this.password = password;
		this.server = server;
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}
	

}
