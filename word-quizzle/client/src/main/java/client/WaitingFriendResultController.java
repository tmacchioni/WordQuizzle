package client;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.PlayerStats;

public class WaitingFriendResultController {

	@FXML
	private Label timeExpiredLabel;

	@FXML
	private Label finishedLabel;

	public void setTimeExpired(boolean value) {
		Platform.runLater(() -> {
			if(value) {
				finishedLabel.setVisible(false);
				timeExpiredLabel.setVisible(true);
			}
		});
	}

	public void showEndChallengeScene(PlayerStats challengerStats, PlayerStats challengedStats) {
		Platform.runLater(() -> {
			FXMLLoader loader = new FXMLLoader(MainLauncher.class.getResource("views/endChallengeView.fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			EndChallengeController controller = loader.<EndChallengeController>getController();
			controller.setGridPanes(challengerStats, challengedStats);

			Stage stage = MainLauncher.getPrimaryStage();
			stage.setScene(new Scene(root));
			stage.show();

		});
	}

}
