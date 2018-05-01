package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

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
	    int rowIdx = 0, colIdx = 0;
		try {
			lblTableName.setText(selectedTable.toUpperCase());
			ResultSet columnResultSet = metaData.getColumns(null, null, selectedTable, null);
			ObservableList<String> columnList = FXCollections.observableArrayList();

			while (columnResultSet.next()) {
				columnList.add(columnResultSet.getString("COLUMN_NAME"));
			}

            for (String column: columnList) {

                Label label = new Label(column);
                label.setStyle("-fx-font-size:20px;-fx-font-weight: bold;");
                gridPaneCreate.add(label,rowIdx,colIdx);
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
