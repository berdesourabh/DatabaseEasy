package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class MainController implements Initializable {

	private Connection connection;

	private DatabaseMetaData metaData;
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
	public GridPane gridPaneCreate;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	private void clearContent() {
		lblError.setText("");
		listTableNames.getItems().clear();
		lblTableName.setText("");
	}

	@FXML
	private void onLoginClick(ActionEvent event) {
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

	private void showTables(String dbName) throws SQLException {
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
			ResultSet columnResultSet = metaData.getColumns(null, null, selectedTable, null);
			ObservableList<String> columnList = FXCollections.observableArrayList();
			List<String> typeList = new ArrayList<>();

			LinkedHashMap<String, String> columnToTypeMap = new LinkedHashMap<>();
			while (columnResultSet.next()) {

				columnToTypeMap.put(columnResultSet.getString("COLUMN_NAME"), columnResultSet.getString("TYPE_NAME"));
				// columnList.add(columnResultSet.getString("COLUMN_NAME"));
				// typeList.add(columnResultSet.getString("TYPE_NAME"));

				// ToDo:[bigserial, varchar, varchar, bool, varchar, timestamptz, varchar,
				// timestamptz, uuid, int8]
				// make Map of column name and data type and traverse through to add it in label
				// and promptedText
				// also we can check type and decide to give textfield or drop down
			}
			int rowIdx = 0, colIdx = 0;
			for (Map.Entry<String, String> map : columnToTypeMap.entrySet()) {
				Label label = new Label(map.getKey() + ":");
				TextField textField = new TextField();
				textField.setPromptText(map.getValue());
				label.setStyle("-fx-font-size:20px;-fx-font-weight: bold;");
				GridPane.setHalignment(label, HPos.CENTER);
				gridPaneCreate.add(label, 0, colIdx);
				GridPane.setHalignment(textField, HPos.LEFT);
				if(!map.getKey().equals("id"))
				gridPaneCreate.add(textField, 1, colIdx);

				colIdx++;
			}
		} catch (Exception e) {
			lblError.setText(e.getMessage());
		}

	}

	@FXML
	private void handleMouseClick(MouseEvent arg0) {
		try {
			gridPaneCreate.getChildren().clear();
			showColumns(listTableNames.getSelectionModel().getSelectedItem());
		} catch (Exception e) {
			lblError.setText(e.getMessage());
		}

	}
}
