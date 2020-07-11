package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChallengeController implements Initializable{

//	private EndChallengeController endChallengeController;

	@FXML
	private AnchorPane countdownPane;

	@FXML
	private Label countdownLabel;

	@FXML
	private AnchorPane challengePane;

	@FXML
	private TextField answerField;

	@FXML
	private Label wordLabel;

	@FXML
	private Label timeLeftLabel;

	@FXML
	private Button sendButton;

	@FXML
	private void sendButtonAction() throws IOException {
		String answer = answerField.getText();
		if(!answer.equals("") || answer != null) {
			ChallengeListener.sendAnswer(answer);
			answerField.clear();
		}
	}

	public void setCountdownLabel(String num) {
		Platform.runLater(() -> {
			this.countdownLabel.setText(num);
		});
	}

	void hideCountdownAndshowChallenge() {
		Platform.runLater(() -> {
			this.countdownPane.setVisible(false);
			this.challengePane.setVisible(true);
		});
	}

	public void setWordLabel(String word) {
		Platform.runLater(() -> {
			this.wordLabel.setText(word);
		});
	}

	public void setTimeLeftLabel(String num) {
		Platform.runLater(() -> {
			this.timeLeftLabel.setText(num);
		});
	}

	//    public String getAnswerField() {
	//    	return this.answerField.getText();
	//    }

	void showWaitingFriendResultScene() {
		Platform.runLater( () -> {
			FXMLLoader loader = new FXMLLoader(MainLauncher.class.getResource("views/waitingFriendResultView.fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Stage stage = MainLauncher.getPrimaryStage();
			stage.setScene(new Scene(root));
			stage.show();
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		answerField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if(t1.equals(""))
					sendButton.setDisable(true);
				else
					sendButton.setDisable(false);
			}
		});

		answerField.setOnKeyPressed((event) -> { 
			if(event.getCode() == KeyCode.ENTER) { try {
				sendButtonAction();
			} catch (IOException e) {
				e.printStackTrace();
			} } 
		});

	}



}
