package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class LoginController implements Initializable{

	/* 
	 * impostazioni di default
	 */
	private int RMIPort = 12346;
	private String hostname = "localhost";
	private int port = 12345;

	private Scene gameScene;

	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	private PasswordField password;

	@FXML
	private Label title;

	@FXML
	private TextField username;

	@FXML
	private Button loginButton;

	@FXML
	private ImageView settings;

	@FXML
	private Hyperlink signup;

	@FXML
	private Button bottone;

	@FXML
	private Label errorLabel;

	@FXML
	private Button exitButton;

	@FXML
	void exitButtonAction() {
		System.exit(0);
	}

	private static LoginController instance;

	public LoginController() {
		instance = this;
	}

	public static LoginController getInstance() {
		return instance;
	}


	public void setNetworkFields(int RMIPort, String hostname, int port) {
		this.RMIPort = RMIPort;
		this.hostname = hostname;
		this.port = port;
	}

	@FXML
	void loginButtonAction() throws IOException {

		FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("views/gameView.fxml"));
		Parent root = fmxlLoader.load();
		this.gameScene = new Scene(root);

		GameController gcon = fmxlLoader.<GameController>getController();

		GameListener listener = new GameListener(hostname, port, username.getText(), password.getText(), gcon);
		Thread x = new Thread(listener);
		x.start();
	}

	@FXML
	void settingsAction(MouseEvent event) throws IOException {

		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("views/settingsView.fxml"));
		Parent root = loader.load();
		SettingsController controller = loader.getController();
		controller.setFields(this.RMIPort, this.hostname, this.port);
		stage.setScene(new Scene(root));
		stage.initStyle(StageStyle.UNDECORATED);
		stage.initModality(Modality.APPLICATION_MODAL);
		calculateCenterPosition(stage);
		stage.showAndWait();
	}

	@FXML
	void signupAction(ActionEvent event) throws IOException {

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("views/signupView.fxml"));
		Parent root = loader.load();

		Scene scene = new Scene(root);

		SignupController controller = loader.getController();
		controller.setRMIPort(RMIPort);
		controller.setHostname(hostname);

		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

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

		window.setScene(scene);
		window.show();

	}


	@FXML
	public void showScene() throws IOException {

		Platform.runLater(() -> {
			Stage stage = (Stage) username.getScene().getWindow();

			stage.setOnCloseRequest((WindowEvent e) -> {
				Platform.exit();
				System.exit(0);
			});
			stage.setScene(this.gameScene);

			//grab your root here
			gameScene.getRoot().setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});

			//move around here
			gameScene.getRoot().setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					stage.setX(event.getScreenX() - xOffset);
					stage.setY(event.getScreenY() - yOffset);
				}
			});

			stage.show();
		});

	}

	public void showErrorDialog(String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Warning!");
			alert.setHeaderText(message);
			alert.setContentText("Please check for firewall issues and\ncheck if the server is running.");
			alert.showAndWait();
		});
	}

	public void showErrorFields() {
		Platform.runLater(() -> {
			errorLabel.setVisible(true);
			password.clear();
		});
	}

	private void calculateCenterPosition(Stage stage) {
		// Calculate the center position of the parent Stage
		Stage primaryStage = MainLauncher.getPrimaryStage();

		ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
			double stageWidth = newValue.doubleValue();
			stage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - stageWidth / 2);
		};
		ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
			double stageHeight = newValue.doubleValue();
			stage.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - stageHeight / 2);   
		};

		stage.widthProperty().addListener(widthListener);
		stage.heightProperty().addListener(heightListener);

		//Once the window is visible, remove the listeners
		stage.setOnShown(e -> {
			stage.widthProperty().removeListener(widthListener);
			stage.heightProperty().removeListener(heightListener);
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		MainLauncher.getPrimaryStage().setOnCloseRequest( e -> exitButtonAction() );

		loginButton.setDisable(true);

		username.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if(t1.equals(""))
					loginButton.setDisable(true);
				else
					if(password.getText().equals("") == false) {
						loginButton.setDisable(false);
					}
			}
		});

		password.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if(t1.equals(""))
					loginButton.setDisable(true);
				else
					if(username.getText().equals("") == false) {
						loginButton.setDisable(false);
					}
			}
		});	

	}
}


