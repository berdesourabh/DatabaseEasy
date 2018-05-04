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
import java.util.UUID;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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

	@FXML
	public AnchorPane anchorPaneCreate;
	
	@FXML
	public TextField txtFieldSearch;
	
	@FXML 
	private List<String> fixedTableList = new ArrayList<>();

	private List<String> columnList = new ArrayList<>();

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
		fixedTableList.addAll(tableList);
		listTableNames.getItems().addAll(tableList);
		lblMainDBName.setText(dbName);

	}

	public void showColumns(String selectedTable) {

		try {
			lblTableName.setText(selectedTable.toUpperCase());
			ResultSet columnResultSet = metaData.getColumns(null, null, selectedTable, null);

			LinkedHashMap<String, String> columnToTypeMap = new LinkedHashMap<>();
			while (columnResultSet.next()) {

				columnToTypeMap.put(columnResultSet.getString("COLUMN_NAME"), columnResultSet.getString("TYPE_NAME"));

				// also we can check type and decide to give textfield or drop down
			}
			int colIdx = 0;
			for (Map.Entry<String, String> map : columnToTypeMap.entrySet()) {
				if (!map.getKey().equals("id")) {
					columnList.add(map.getKey());
					Label label = new Label(map.getKey() + ":");
					switch (map.getValue()) {
					case "int8":
						TextField textField = new TextField();
						textField.setPromptText("number");
						textField.setId(map.getKey());
						GridPane.setHalignment(textField, HPos.LEFT);
						gridPaneCreate.add(textField, 1, colIdx);
						break;
					case "varchar":
						TextField textFieldvarchar = new TextField();
						textFieldvarchar.setPromptText("String");
						textFieldvarchar.setId(map.getKey());
						GridPane.setHalignment(textFieldvarchar, HPos.LEFT);
						gridPaneCreate.add(textFieldvarchar, 1, colIdx);
						break;
					case "char":
						TextField textFieldchar = new TextField();
						textFieldchar.setPromptText("String");
						textFieldchar.setId(map.getKey());
						GridPane.setHalignment(textFieldchar, HPos.LEFT);
						gridPaneCreate.add(textFieldchar, 1, colIdx);
						break;
					case "bool":
						ObservableList<String> options = 
					    FXCollections.observableArrayList(
					       "True",
					       "False"
					    );
						ComboBox<String> comboBox = new ComboBox<>(options);
						comboBox.setValue("True");
						comboBox.setId(map.getKey());
						comboBox.setPrefWidth(150);
						GridPane.setHalignment(comboBox, HPos.LEFT);
						gridPaneCreate.add(comboBox, 1, colIdx);
						break;
					case "date":
						DatePicker datePicker = new DatePicker();
						datePicker.setValue(LocalDate.now());
						datePicker.setPrefWidth(150);
						GridPane.setHalignment(datePicker, HPos.LEFT);
						gridPaneCreate.add(datePicker, 1, colIdx);
						break;
					case "uuid":
						TextField textFielduuid = new TextField();
						textFielduuid.setPrefWidth(30);
						textFielduuid.setPromptText(map.getValue());
						textFielduuid.setText(UUID.randomUUID().toString());
						textFielduuid.setId(map.getKey());
						GridPane.setHalignment(textFielduuid, HPos.LEFT);
						gridPaneCreate.add(textFielduuid, 1, colIdx);
						break;
						
					default:
						break;
					}
					
					label.setStyle("-fx-font-size:20px;-fx-font-weight: bold;");
					GridPane.setHalignment(label, HPos.RIGHT);
					gridPaneCreate.add(label, 0, colIdx);
					
					colIdx++;
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
			StringBuffer query = new StringBuffer(" INSERT INTO ").append(lblTableName.getText()).append(" ");
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
