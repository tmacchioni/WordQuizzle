package client;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class SettingsController {

	@FXML
	private TextField rmiTextField;

	@FXML
	private TextField hostnameTextField;

	@FXML
	private TextField portTextField;

	@FXML
	private Button done;

	@FXML
	void doneButtonAction() throws IOException {

		int RMIPort = Integer.parseInt(rmiTextField.getText());
		String hostname = hostnameTextField.getText();
		int port = Integer.parseInt(portTextField.getText());

		LoginController.getInstance().setNetworkFields(RMIPort, hostname, port);

		done.getScene().getWindow().hide();

	}
	
	public void setFields(int RMIPort, String hostname, int port) {
		this.rmiTextField.setText(String.valueOf(RMIPort));
		this.hostnameTextField.setText(hostname);
		this.portTextField.setText(String.valueOf(port));
	}
	
	

}
