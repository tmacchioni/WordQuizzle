package client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class InvitationAlertController {

	@FXML
	private Button acceptButton;

	@FXML
	private Button rejectButton;

	@FXML
	private Label challengerLabel;

	@FXML
	private Label pointsChallengerLabel;

	private boolean acceptButtonWasClicked;


	void setFields(String challenger, String score) {
		challengerLabel.setText(challenger);
		pointsChallengerLabel.setText(score);
	}

	boolean acceptButtonWasClicked() {
		return acceptButtonWasClicked;
	}
	
	@FXML
	void acceptButtonAction() throws IOException {
		acceptButton.getScene().getWindow().hide();
		acceptButtonWasClicked = true;
	}

	@FXML
	void rejectButtonAction() throws IOException {
		rejectButton.getScene().getWindow().hide();
		acceptButtonWasClicked = false;
	}


}
