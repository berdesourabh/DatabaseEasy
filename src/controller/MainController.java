package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class MainController implements Initializable {

	Connection connection;

	DatabaseMetaData metaData;
	@FXML
	public TextField txtUsername;

	@FXML
	public TextField txtPassword;

	@FXML
	public TextField txtDatabase;

	@FXML
	public TextField txtServerUrl;

	@FXML
	public Label lblError;

	@FXML
	public Label lblMainDBName;

	@FXML
	public Label lblTableName;

	@FXML
	public ListView<String> listTableNames;

	@FXML
	public ListView<String> listColumnNames;
	
	@FXML
	public GridPane createPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void clearContent() {
		lblError.setText("");
		listTableNames.getItems().clear();
		listColumnNames.getItems().clear();
		lblTableName.setText("");
	}

	@FXML
	public void onLoginClick(ActionEvent event) {
		clearContent();
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(
					"jdbc:postgresql://" + txtServerUrl.getText() + ":5432/" + txtDatabase.getText(),
					txtUsername.getText(), txtPassword.getText());
			if (connection.isValid(0)) {
				showTables(txtDatabase.getText());
			}
		} catch (

		Exception e) {
			lblError.setText(e.getLocalizedMessage());
		}

	}

	public void showTables(String dbName) throws SQLException {
		metaData = connection.getMetaData();
		String[] types = { "TABLE" };
		ResultSet rs = metaData.getTables(null, "public", "%", types);
		ObservableList<String> tableList = FXCollections.observableArrayList();

		while (rs.next()) {
			tableList.add(rs.getString(3));
		}
		listTableNames.getItems().addAll(tableList);
		lblMainDBName.setText(dbName);

	}

	public void showColumns(String selectedTable) {
		try {
			lblTableName.setText(selectedTable.toUpperCase());
			listColumnNames.getItems().clear();
			ResultSet columnResultSet = metaData.getColumns(null, null, selectedTable, null);
			ObservableList<String> columnList = FXCollections.observableArrayList();

			while (columnResultSet.next()) {
				columnList.add(columnResultSet.getString("COLUMN_NAME"));
			}
			
			listColumnNames.getItems().addAll(columnList);

		} catch (Exception e) {
			lblError.setText(e.getMessage());
		}

	}

	@FXML
	public void handleMouseClick(MouseEvent arg0) {
		try {
			showColumns(listTableNames.getSelectionModel().getSelectedItem());
		} catch (Exception e) {
			lblError.setText(e.getMessage());
		}

	}
}
