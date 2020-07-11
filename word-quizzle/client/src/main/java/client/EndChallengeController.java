package client;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import utils.PlayerStats;

public class EndChallengeController {

	@FXML
	private Label wonLabel;

	@FXML
	private Label lostLabel;

	@FXML
	private Label drawLabel;
	
	@FXML
	private Label friendLabel;

	@FXML
	private Button backButton;

	@FXML
	private GridPane gridPane;

	private int totalPoints;


	@FXML
	void backButtonAction(ActionEvent event) throws IOException {
		Platform.runLater(() -> {
			GameController gameController = GameController.getInstance();
			int previousPoints = Integer.valueOf(gameController.getPoints().getText()).intValue();
			totalPoints += previousPoints;
			gameController.setPoints(String.valueOf(totalPoints));

			Stage stage = MainLauncher.getPrimaryStage();
			stage.setScene(gameController.getScene());
			stage.show();
		});
	}


	void setGridPanes(PlayerStats challengerStats, PlayerStats challengedStats) {
		Platform.runLater(() -> {			



			friendLabel.setText(challengedStats.getUsername());


			LinkedHashMap<String, String> challengerMap = challengerStats.getResultsMap();
			LinkedHashMap<String, String> challengedMap = challengedStats.getResultsMap();

			Set<String> keySet = null;
			if(challengerStats.hasWon()) {
				wonLabel.setVisible(true);
				keySet = challengerMap.keySet();
			}
			else if(challengedStats.hasWon()){ 
				lostLabel.setVisible(true);
				keySet = challengedMap.keySet();
			} else {
				drawLabel.setVisible(true);
				keySet = challengerMap.keySet();
			}
			 
			int row = 0;

			Iterator<String> iterator = keySet.iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				String challengerValue = challengerMap.get(key);
				String challengedValue = challengedMap.get(key);

				gridPane.add(new Label(key), 0, row);
				gridPane.add(new Label(challengerValue), 1, row);
				gridPane.add(new Label(challengedValue), 2, row);

				row++;
			}

			
			totalPoints = challengerStats.getTotPoints();

			Label totalLabel = new Label("TOTAL");
			totalLabel.setStyle("-fx-font-size: 20px");

			Label challengerTotalLabel = new Label(challengerStats.getTotPointsAsString());
			challengerTotalLabel.setStyle("-fx-font-size: 20px");

			Label challengedTotalLabel = new Label(challengedStats.getTotPointsAsString());
			challengedTotalLabel.setStyle("-fx-font-size: 20px");

			row++;
			gridPane.add(totalLabel, 0, row);
			gridPane.add(challengerTotalLabel, 1, row);
			gridPane.add(challengedTotalLabel, 2, row);

		});
	}

}
