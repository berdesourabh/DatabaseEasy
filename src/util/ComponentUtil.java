package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

public class ComponentUtil {

	public static List<String> getNullConstraintColumns(String selectedTable, Connection connection)
			throws SQLException {
		if (connection.isValid(0)) {
			List<String> columnNames = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(
					"SELECT column_name FROM  INFORMATION_SCHEMA.COLUMNS where table_name = ? and is_nullable = 'NO'");
			st.setString(1, selectedTable);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				columnNames.add(rs.getString("column_name"));

			}
			return columnNames;
		}
		return null;

	}

	public static boolean validateNotNull(String column, List<String> mandetoryColumns, String value) {
		if (mandetoryColumns.contains(column) && Strings.isNullOrEmpty(value)) {
			return false;
		}
		return true;
	}

	public static List<Credential> processCredentialList() throws IOException {
		List<Credential> credentialList = new ArrayList<>();
		String line = "";
		String cvsSplitBy = ",";
		try {
			String currentDirectory = new File("").getAbsolutePath();
			BufferedReader br = new BufferedReader(new FileReader(currentDirectory + "/resource/data.csv"));

			while ((line = br.readLine()) != null) {
				if (line != "") {
					String[] data = line.split(cvsSplitBy);
					if (line != null && (!line.trim().equals(""))) {
						Credential credential = new Credential(data[0], data[1], data[2]);
						credentialList.add(credential);
					}
				}
			}
			return credentialList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void addCredentials(String username, String server, String database) throws IOException {
		String currentDirectory = new File("").getAbsolutePath();
		List<Credential> credentialList = processCredentialList();
		boolean isEqual = false;

		for (Credential credential : credentialList) {
			if (credential.getUsername().equals(username) && credential.getServer().equals(server)
					&& credential.getDatabase().equals(database)) {
				isEqual = true;
			}
		}

		if (!isEqual) {
			FileWriter pw = new FileWriter(currentDirectory + "/resource/data.csv", true);

			pw.append(username);
			pw.append(",");
			pw.append(server);
			pw.append(",");
			pw.append(database);
			pw.append("\n");
			pw.close();
		}

	}
}
