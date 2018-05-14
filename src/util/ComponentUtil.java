package util;

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
}
