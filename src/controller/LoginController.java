package controller;

import java.sql.Connection;
import java.sql.DriverManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class LoginController {
	@FXML
	public TextField txtLoginUsername;

	@FXML
	public TextField txtLoginPassword;

	@FXML
	public TextField txtLoginDB;

	@FXML
	public TextField txtLoginServerUrl;

	@FXML
	public Label lblLoginError;

	@FXML
	public AnchorPane loginPane;

	@FXML
	public void onLoginClick(ActionEvent event) {
		Connection c = null;
		lblLoginError.setText("");
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection(
					"jdbc:postgresql://" + txtLoginServerUrl.getText() + ":5432/" + txtLoginDB.getText(),
					txtLoginUsername.getText(), txtLoginPassword.getText());
			if (c.isValid(0)) {
				FXMLLoader loader = new FXMLLoader();
				AnchorPane pane = loader.load(getClass().getResource("/view/Main.fxml").openStream());
				loginPane.getChildren().setAll(pane);
				MainController mainController = (MainController)loader.getController();
				mainController.showTables(c, txtLoginDB.getText());
			}
		} catch (

		Exception e) {
			lblLoginError.setText(e.getMessage());
		}

	}
}
