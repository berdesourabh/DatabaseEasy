package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class MainController implements Initializable {
	@FXML
	public Label lblMainDBName;

	@FXML
	public ListView<String> listTableNames;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	public void showTables(Connection con, String dbName) throws SQLException {
		DatabaseMetaData md = con.getMetaData();
		String[] types = { "TABLE" };
		ResultSet rs = md.getTables(null, "public", "%", types);
		List<String> tables = new ArrayList<>();

		while (rs.next()) {
			tables.add(rs.getString(3));
		}
		listTableNames.getItems().addAll(tables);
		lblMainDBName.setText(dbName);

	}

}
