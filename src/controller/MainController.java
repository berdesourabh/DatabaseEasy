package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import util.Constants;

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

	@FXML
	public AnchorPane anchorPaneCreate;

	@FXML
	public TextField txtFieldSearch;

	@FXML
	private List<String> fixedTableList = new ArrayList<>();

	private List<String> columnList = new ArrayList<>();

	@FXML
	private ScrollPane createScrollPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	private void clearContent() {
		lblError.setText("");
		listTableNames.getItems().clear();
		lblTableName.setText("");
		gridPaneCreate.getChildren().clear();
	}

	@FXML
	private void onLoginClick(ActionEvent event) {
		clearContent();
		try {
			Class.forName(Constants.POSTGRES_DRIVER);
			connection = DriverManager.getConnection(
					Constants.POSTGRES + txtServerUrl.getText() + Constants.DEFAULT_PORT + txtDatabase.getText(),
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
		String[] types = { Constants.TABLE };
		ResultSet rs = metaData.getTables(null, Constants.PUBLIC, "%", types);
		ObservableList<String> tableList = FXCollections.observableArrayList();

		while (rs.next()) {
			tableList.add(rs.getString(3));
		}
		fixedTableList.addAll(tableList);
		listTableNames.getItems().addAll(tableList);
		lblMainDBName.setText(dbName);

	}

	public void showColumns(String selectedTable) {

		try {
			lblTableName.setText(selectedTable);
			ResultSet columnResultSet = metaData.getColumns(null, null, selectedTable, null);

			LinkedHashMap<String, String> columnToTypeMap = new LinkedHashMap<>();
			while (columnResultSet.next()) {
				columnToTypeMap.put(columnResultSet.getString(Constants.COLUMN_NAME), columnResultSet.getString(Constants.TYPE_NAME));
			}
			int rowIdx = 0;
			for (Map.Entry<String, String> map : columnToTypeMap.entrySet()) {
				if (!map.getKey().equals(Constants.ID)) {
					columnList.add(map.getKey());
					Label label = new Label(map.getKey() + ":");
					label.setStyle("-fx-font-size:20px;-fx-font-weight: bold;");
					GridPane.setHalignment(label, HPos.RIGHT);
					gridPaneCreate.add(label, 0, rowIdx);

					if (map.getValue().equalsIgnoreCase(Constants.INT4) || map.getValue().equalsIgnoreCase(Constants.INT8)
							|| map.getValue().equalsIgnoreCase(Constants.FLOAT8)
							|| map.getValue().equalsIgnoreCase(Constants.NUMERIC)) {
						TextField textField = new TextField();
						textField.setPromptText(Constants.NUMBER);
						textField.setId(map.getKey());
						GridPane.setHalignment(textField, HPos.LEFT);
						gridPaneCreate.add(textField, 1, rowIdx);
					} else if (map.getValue().equalsIgnoreCase(Constants.VARCHAR) || map.getValue().equalsIgnoreCase(Constants.CHAR)) {
						TextField textFieldvarchar = new TextField();
						textFieldvarchar.setPromptText(Constants.STRING);
						textFieldvarchar.setId(map.getKey());
						GridPane.setHalignment(textFieldvarchar, HPos.LEFT);
						gridPaneCreate.add(textFieldvarchar, 1, rowIdx);
					} else if (map.getValue().equalsIgnoreCase(Constants.TEXT) || map.getValue().equalsIgnoreCase(Constants.BYTEA)) {
						TextArea textAreaText = new TextArea();
						textAreaText.setPromptText(Constants.TEXT);
						textAreaText.setId(map.getKey());
						GridPane.setHalignment(textAreaText, HPos.LEFT);
						gridPaneCreate.add(textAreaText, 1, rowIdx);
					} else if (map.getValue().equalsIgnoreCase(Constants.BOOL)) {
						ObservableList<String> options = FXCollections.observableArrayList("", Constants.TRUE, Constants.FALSE);
						ComboBox<String> comboBox = new ComboBox<>(options);
						comboBox.setValue("");
						comboBox.setId(map.getKey());
						comboBox.setPrefWidth(150);
						GridPane.setHalignment(comboBox, HPos.LEFT);
						gridPaneCreate.add(comboBox, 1, rowIdx);
					} else if (map.getValue().equalsIgnoreCase(Constants.DATE) || map.getValue().equalsIgnoreCase(Constants.TIME)
							|| map.getValue().equalsIgnoreCase(Constants.TIMESTAMPTZ)) {
						DatePicker datePicker = new DatePicker();
						datePicker.setValue(LocalDate.now());
						datePicker.setPrefWidth(150);
						GridPane.setHalignment(datePicker, HPos.LEFT);
						gridPaneCreate.add(datePicker, 1, rowIdx);
					} else if (map.getValue().equalsIgnoreCase(Constants.UUID)) {
						TextField textFielduuid = new TextField();
						textFielduuid.setPrefWidth(30);
						textFielduuid.setPromptText(Constants.DEFAULT_UUID);
						textFielduuid.setId(map.getKey());
						GridPane.setHalignment(textFielduuid, HPos.LEFT);
						gridPaneCreate.add(textFielduuid, 1, rowIdx);
					}
					rowIdx++;
				}
			}
		} catch (Exception e) {
			lblError.setText(e.getMessage());
		}
	}

	@FXML
	public void onCreateSubmit(ActionEvent event) {
		if (!columnList.isEmpty()) {
			StringBuilder columBuffer = new StringBuilder("(");
			StringBuilder valueBuffer = new StringBuilder("(");
			StringBuilder query = new StringBuilder(" INSERT INTO ").append(lblTableName.getText()).append(" ");
			String lastColumn = columnList.get(columnList.size() - 1);
			ObservableList<Node> childs = gridPaneCreate.getChildren();
			for (Node node : childs) {
				for (String column : columnList) {
					if (node instanceof TextField && node.getId().equals(column)) {
						TextField textField = (TextField) node;
						String text = textField.getText();
						String columnName = textField.getId();
						columBuffer.append(columnName);
						valueBuffer.append(text);
						if (!column.equals(lastColumn)) {
							columBuffer.append(", ");
							valueBuffer.append(", ");
						}
						break;
					}
				}
			}
			columBuffer.append(" )");
			valueBuffer.append(" )");
			query.append(columBuffer).append(" VALUES ").append(valueBuffer);
			ObservableList<Node> anchorChilds = anchorPaneCreate.getChildren();
			for (Node node : anchorChilds) {
				if (node instanceof TextArea) {
					TextArea area = (TextArea) node;
					area.setText(query.toString());
				}
			}
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

	@FXML
	private void handleSearch() {
		String searchTerm = txtFieldSearch.getText();
		List<String> tables = new ArrayList<>();
		tables.addAll(fixedTableList);
		tables = tables.stream().filter(table -> table.startsWith(searchTerm)).collect(Collectors.toList());
		ObservableList<String> filteredList = FXCollections.observableArrayList(tables);
		listTableNames.setItems(filteredList);
	}

}
