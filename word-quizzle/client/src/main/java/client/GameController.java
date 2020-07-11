package client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import utils.Friend;

public class GameController implements Initializable {

	private WaitingAlertController waitingAlertController;

	private Image tick = new Image(getClass().getResource("icons/tick.png").toString());
	private Image cross = new Image(getClass().getResource("icons/cross.png").toString());


	@FXML
	private Label title;

	@FXML
	private TableView<Friend> friendsTable;

	@FXML
	private TableColumn<Friend, String> nameColumn;

	@FXML
	private TableColumn<Friend, String> scoreColumn;

	@FXML
	private TableColumn<Friend, String> statusColumn;

	@FXML
	private Button playButton;

	@FXML
	private Button logoutButton;

	@FXML
	private Button addFriendButton;

	@FXML
	private Label username;

	@FXML
	private Label points;

	@FXML
	private TextField friendUsername;

	@FXML
	private Label errorLabel;

	@FXML
	private ImageView imageView;
	
	@FXML
    private Button exitButton;
	
	private Scene challengeScene;
	
	private Scene thisScene;
	
	private static GameController instance;

	private double xOffset = 0;
	private double yOffset = 0;
	
	public GameController() {
		instance = this;
	}

	static GameController getInstance() {
		return instance;
	}

	public Scene getScene() {
		return thisScene;
	}

    private @FXML
    void exitButtonAction() throws IOException {
    	GameListener.logout();
    	System.exit(0);
    }


	public Label getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username.setText(username);
	}

	public Label getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points.setText(points);
	}

	void updateRow(Friend friend) {//aggiorna un amico se esiste o lo aggiunge se non esiste

		ObservableList<Friend> list = friendsTable.getItems();
		int i = list.indexOf(friend);
		if(i != -1) {
			list.remove(i);
		}
		list.add(friend);
		friendsTable.setItems(list);
		friendsTable.refresh();

	}

	void addRows(Collection<Friend> friends) {

		ObservableList<Friend> list = friendsTable.getItems();
		list.addAll(friends);
		friendsTable.setItems(list);
		friendsTable.refresh();
	}



	public void setFriendsOnline(ArrayList<Friend> onlineFriends) {

		ObservableList<Friend> list = friendsTable.getItems();
		list.removeAll(onlineFriends);
		list.addAll(onlineFriends);		
		friendsTable.setItems(list);
		friendsTable.refresh();

	}


	void hideWaitingAlertScene() {
		Platform.runLater(() -> {
			if(waitingAlertController != null) {
				waitingAlertController.hideScene();
				waitingAlertController = null;
			}
		});
	}

	void startChallenge(String username, String score, String hostname, int port) throws IOException {

		Platform.runLater(() -> {

			hideWaitingAlertScene();

			FXMLLoader loader = new FXMLLoader(MainLauncher.class.getResource("views/challengeView.fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {
				logoutButtonAction();
			}
			
			challengeScene = new Scene(root);


			ChallengeController challengeController = loader.<ChallengeController>getController();
			
			loader = new FXMLLoader(MainLauncher.class.getResource("views/waitingFriendResultView.fxml"));
			try {
				loader.load();
			} catch (IOException e) {
				logoutButtonAction();
			}
			
			WaitingFriendResultController endChallengeController = loader.<WaitingFriendResultController>getController();

			ChallengeListener listener = new ChallengeListener(username, hostname, port, challengeController, endChallengeController);
			Thread x = new Thread(listener);
			x.start();
			
		});
	}
	
	void showChallenge() {
		Platform.runLater(() -> {
			Stage stage = (Stage) username.getScene().getWindow();

			stage.setOnCloseRequest((WindowEvent e) -> {
				Platform.exit();
				System.exit(0);
			});
			stage.setScene(this.challengeScene);
			
			//grab your root here
			challengeScene.getRoot().setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});

			//move around here
			challengeScene.getRoot().setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					stage.setX(event.getScreenX() - xOffset);
					stage.setY(event.getScreenY() - yOffset);
				}
			});
			thisScene = playButton.getScene();
			stage.show();
		});
	}

	@FXML
	void addFriendButtonAction(ActionEvent event) throws IOException {
		String friendToAdd = friendUsername.getText();

		assert(friendToAdd != null);
		
		GameListener.aggiungi_amico(friendToAdd);
	}

	@FXML
	private void logoutButtonAction()  {
		try {
			GameListener.logout();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
	}

	@FXML
	void playButtonAction(ActionEvent event) throws IOException {


		Friend friend = friendsTable.getSelectionModel().getSelectedItem();
		GameListener.sfida(friend);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("views/waitingAlertView.fxml"));
		Parent root = loader.load();

		waitingAlertController = loader.<WaitingAlertController>getController();

		Stage stage = new Stage();
		stage.setScene(new Scene(root));
		stage.initStyle(StageStyle.UNDECORATED);
		stage.initModality(Modality.APPLICATION_MODAL);

		calculateCenterPosition(stage);

		stage.showAndWait();


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

	@FXML
	void enablePlayButton() {

		Friend selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
		if(selectedFriend == null) return;
		if(selectedFriend.getStatus().equals("online")) {
			playButton.setDisable(false);
		} else {
			playButton.setDisable(true);
		}
	}

	@FXML
	void hideMsg() {
		errorLabel.setVisible(false);
		imageView.setVisible(false);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		MainLauncher.getPrimaryStage().setOnCloseRequest( e -> {
			try {
				exitButtonAction();
			} catch (IOException e1) {
				e1.printStackTrace();
				logoutButtonAction();
			}
		} );
		
		
		waitingAlertController=null;

		nameColumn.setCellValueFactory(new PropertyValueFactory<>("nickname"));
		scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

		playButton.setDisable(true);
		addFriendButton.setDisable(true);

		friendUsername.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if(t1.equals(""))
					addFriendButton.setDisable(true);
				else
					addFriendButton.setDisable(false);
			}
		});

		errorLabel.setVisible(false);
		imageView.setVisible(false);
	}

	void showInvitationAlert(String challenger, String score, String hostname, int port) throws IOException, InterruptedException, ExecutionException {
		Platform.runLater(() -> {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("views/invitationAlertView.fxml"));
			Parent root = null;
			try {
				root = loader.load();
			} catch (IOException e) {
				logoutButtonAction();
				e.printStackTrace();
			}

			InvitationAlertController controller = loader.<InvitationAlertController>getController();
			controller.setFields(challenger, score);

			Stage stage = new Stage();
			Scene scene = new Scene(root);
			stage.setResizable(false);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);

			calculateCenterPosition(stage);
			
			stage.showAndWait();

			if (controller.acceptButtonWasClicked() == true) {
				try {
					startChallenge(username.getText(), points.getText(), hostname, port);
				} catch (IOException e) {
					e.printStackTrace();
					logoutButtonAction();
				}
			}

		});
	}
	
	public void updatePoints(String value) {
		Platform.runLater(() -> {
			points.setText(value);
		});
	}

	void logoutScene() {
		Platform.runLater(() -> {
			FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("views/loginView.fxml"));
			Parent root = null;
			try {
				root = fmxlLoader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Stage stage = MainLauncher.getPrimaryStage();
			Scene scene = new Scene(root);
			stage.setResizable(false);
			stage.setScene(scene);
			stage.show();
		});
	}

	void showErrorMsg(String msg) {
		Platform.runLater(() -> {
			errorLabel.setText(msg);
			imageView.setImage(cross);
			errorLabel.setVisible(true);
			imageView.setVisible(true);
		});
	}

	void showTick() {
		Platform.runLater(() -> {
			friendUsername.clear();
			errorLabel.setVisible(false);
			imageView.setImage(tick);
			imageView.setVisible(true);
		});
	}
}
