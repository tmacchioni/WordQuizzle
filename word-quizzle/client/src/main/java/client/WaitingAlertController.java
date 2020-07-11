package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WaitingAlertController {

    @FXML
    private Label waitingLabel;


    public void hideScene() {
    	waitingLabel.getScene().getWindow().hide();
    }

}
