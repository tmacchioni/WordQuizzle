package client;

import java.io.IOException;
import java.rmi.Naming;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.Message;
import utils.MessageSubType;
import utils.MessageType;
import utils.RemoteRegistrationInterface;

public class SignupController {

	private int RMIPort;
	private String hostname;

	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	private PasswordField password;

	@FXML
	private TextField username;

	@FXML
	private Button signupButton;

	@FXML
	private Label regError;

	@FXML
	private Label usernameErr;

	@FXML
	private Label passwordErr;

	@FXML
	private Button goBackButton;

	@FXML
	private ImageView done;

	@FXML
	private ImageView error;

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setRMIPort(int RMIPort) {
		this.RMIPort = RMIPort;
	}

	@FXML
	void goBackButtonAction(ActionEvent event) throws IOException {

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("views/loginView.fxml"));
		Parent root = loader.load();

		Scene scene = new Scene(root);

		//grab your root here
		scene.getRoot().setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});

		//move around here
		scene.getRoot().setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				MainLauncher.getPrimaryStage().setX(event.getScreenX() - xOffset);
				MainLauncher.getPrimaryStage().setY(event.getScreenY() - yOffset);
			}
		});

		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

		window.setScene(scene);
		window.show();

	}

	@FXML
	void signupButtonAction() {

		try {
			String addressRMI = String.format("rmi://%s:%d/", hostname, RMIPort);
			String name = "REG-SERVER";
			RemoteRegistrationInterface rri = (RemoteRegistrationInterface) Naming.lookup(addressRMI + name);
			Message response = rri.registra_utente(username.getText(), password.getText());

			if(response.getType().equals(MessageType.OK)) {
				regError.setVisible(false);
				usernameErr.setVisible(false);
				passwordErr.setVisible(false);
				error.setVisible(false);
				username.clear();
				password.clear();
				done.setVisible(true);
			} else {

				MessageSubType subtype = response.getSubtype();

				switch(subtype) {
				case NAME_FIELD:
					done.setVisible(false);
					error.setVisible(true);
					regError.setVisible(false);
					usernameErr.setText( (String) response.getMsg());
					usernameErr.setVisible(true);
					break;
				case PASS_FIELD:
					done.setVisible(false);
					error.setVisible(true);
					passwordErr.setText( (String) response.getMsg());
					passwordErr.setVisible(true);
					break;
				case EXIST:
					done.setVisible(false);
					error.setVisible(true);
					usernameErr.setVisible(false);
					passwordErr.setVisible(false);
					regError.setVisible(true);
					break;
				default:
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}

}
