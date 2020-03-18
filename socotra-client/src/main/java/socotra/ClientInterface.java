import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * class for client user interface once logged into account
 *
 */
public class ClientInterface extends Application{

	private final int x = 800;
	private final int y = 500;
	//position of items within shell
	private final Pos center = Pos.CENTER;
	//margin around components of shell
	Insets margin = new Insets(10, 10, 10, 10);
	// insets = top, right, bottom, left
	
	@Override
	public void start(Stage stage) throws Exception {
		TabPane options = new TabPane();
		
		Tab currentChat = new Tab("Chats");
		Tab friendsList = new Tab("Friends List");
		Tab groupChat = new Tab("Group Chat", new Label("Show group chats here"));
		Tab settings = new Tab("Settings");
		
		options.getTabs().add(currentChat);
		options.getTabs().add(friendsList);
		options.getTabs().add(groupChat);
		options.getTabs().add(settings);
		
		Button newchat = new Button();
		Button addfriend = new Button();
		if (currentChat.getContent() == null) {
			ImageView newchatimage = new ImageView("newchat.png");
			newchat.setAlignment(center);
			newchat.setGraphic(newchatimage);
			currentChat.setContent(newchat);
		}
		//else set content of tab to information from database to select current conversations between different people
		
		EventHandler<MouseEvent> openNewChat = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent standard) {
				 if (newchat != null) {
					 //open new chat
				 }
			}
		};
		
		EventHandler<MouseEvent> addnewfriend = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent standard) {
				 if (addfriend != null) {
					 //add new friend
					 Stage newfriend = new Stage();
					 newfriend.show();//new chat with person on left
				 }
			}
		};
		if (friendsList.getContent() == null) {
			ImageView addfriendimage = new ImageView("addfriend.png");
			addfriend.setAlignment(center);
			addfriend.setGraphic(addfriendimage);
			friendsList.setContent(addfriend);
		}
		//else set content of tab to information from database about friends list
		
		BorderPane pane = new BorderPane();
		Button snakeGame = new Button("Click here to play the \n classic snake game!");
		snakeGame.setPrefSize(200, 100);
		snakeGame.setAlignment(center);
		snakeGame.setLineSpacing(5);
		snakeGame.setPadding(margin);
		GridPane playGame = new GridPane();
		playGame.add(snakeGame, 2, 2);
		
		EventHandler<MouseEvent> openGame = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent game) {
				 if (snakeGame.onMouseClickedProperty() != null) {
					 //create popup to open game in new window
					 Stage popupsnakegame = new Stage();
					 popupsnakegame.show(); //Jianxiang's code
				 }
			}
		};
				 
		VBox tabs = new VBox(options, playGame);
		pane.setRight(tabs);
		
		BorderPane border = new BorderPane();
		VBox main = new VBox();
		GridPane message = new GridPane();
		GridPane inputs = new GridPane();
		
		ScrollPane messageField = new ScrollPane();
		messageField.autosize();
		messageField.setMinHeight(y - 100);
		messageField.setMinWidth(x - 270);
		Button emojis = new Button();
		emojis.autosize();
		Button sendTextMessage = new Button("Send");
		sendTextMessage.autosize();
		Button sendAudioMessage = new Button("Send audio");
		sendAudioMessage.autosize();
		Button playAudioMessage = new Button("Play audio");
		playAudioMessage.autosize();
		Button startRecording = new Button("Start recording");
		startRecording.autosize();
		Button stopRecording = new Button("Stop recording");
		stopRecording.autosize();
		main.getChildren().add(messageField);
		main.snappedBottomInset();
		main.getChildren().add(message);
		main.getChildren().add(inputs);
		sendTextMessage.setAlignment(Pos.CENTER_RIGHT);
		message.setAlignment(Pos.BASELINE_RIGHT);
		inputs.setPadding(margin);
		message.add(sendTextMessage, 0, 0);
		message.add(emojis, 1, 0);
		inputs.setAlignment(Pos.CENTER);
		inputs.add(startRecording, 1, 1);
		inputs.add(stopRecording, 1, 2);
		inputs.add(sendAudioMessage, 2, 1);
		inputs.add(playAudioMessage, 2, 2);
		inputs.setHgap(5);
		inputs.setVgap(5);
		
		border.setLeft(main);
		border.setRight(pane);
		
		Scene scene = new Scene(border, x, y);
		
		Button nightmode = new Button();
		ImageView night = new ImageView("nighticon.png");
		night.maxHeight(5);
		night.maxWidth(5);
		night.setPreserveRatio(true);
		nightmode.setGraphic(night);
		Button defaultC = new Button("Return to default");
		Button logout = new Button("Log out of your account");
		Button deleteAccount = new Button("Remove account permanently");
		
		
		MenuItem colour1 = new MenuItem("Blue");
		MenuItem colour2 = new MenuItem("Red");
		MenuItem colour3 = new MenuItem("Green");
		MenuItem colour4 = new MenuItem("Purple");
		MenuItem colour5 = new MenuItem("Orange");
		
		MenuButton colourPicker = new MenuButton("Colour Scheme", null, colour1, colour2, colour3, colour4, colour5);
		
		EventHandler<MouseEvent> defaultColourSetting = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent standard) {
				 if (defaultC.onMouseClickedProperty() != null) {
					 scene.getStylesheets().remove("dark.css");			
						scene.getStylesheets().remove("blue.css");
						scene.getStylesheets().remove("red.css");
						scene.getStylesheets().remove("green.css");
						scene.getStylesheets().remove("purple.css");
						scene.getStylesheets().remove("orange.css");
						nightmode.setText("Nightmode");	
					
				 }
			}
		};
		
		EventHandler<MouseEvent> darkMode = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent background) {
				if (nightmode.onMouseClickedProperty() != null) {
					scene.getStylesheets().add("dark.css");
				}
			}
		};
		
		EventHandler<MouseEvent> logoutOfAccount = new EventHandler<MouseEvent>() {
			public void handle (MouseEvent outAccount) {
				if (logout.onMouseClickedProperty() != null) {
					//go to login interface
				}
			}
		};
		
		EventHandler<MouseEvent> delete = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent removeAccount) {
				if (deleteAccount.onMouseClickedProperty() != null) {
					//remove account from database
				}
			}
		};
		
		colour1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent c1) {
				if (colour1.getOnAction() != null) {
					scene.getStylesheets().add("blue.css");
				}
			}
		});
		
		colour2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent c1) {
				if (colour2.getOnAction() != null) {
					scene.getStylesheets().add("red.css");
				}
			}
		});
		
		colour3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent c1) {
				if (colour3.getOnAction() != null) {
					scene.getStylesheets().add("green.css");
				}
			}
		});
		
		colour4.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent c1) {
				if (colour4.getOnAction() != null) {
					scene.getStylesheets().add("purple.css");
				}
			}
		});
		
		colour5.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle (ActionEvent c1) {
				if (colour5.getOnAction() != null) {
					scene.getStylesheets().add("orange.css");
				}
			}
		});
		
		HBox colourSet = new HBox(colourPicker);
		
		newchat.setOnMouseClicked(openNewChat);
		addfriend.setOnMouseClicked(addnewfriend);
		snakeGame.setOnMouseClicked(openGame);
		nightmode.setMaxSize(150, 50);
		nightmode.setOnMouseClicked(darkMode);
		nightmode.setAlignment(center);
		defaultC.setMaxSize(150, 50);
		defaultC.setOnMouseClicked(defaultColourSetting);
		defaultC.setAlignment(center);
		nightmode.setMaxSize(50, 50);
		nightmode.setOnMouseClicked(darkMode);
		nightmode.setAlignment(center);
		deleteAccount.setMaxSize(200, 50);
		deleteAccount.setOnMouseClicked(delete);
		deleteAccount.setAlignment(center);
		colourSet.setMaxSize(150, 50);
		colourSet.setAlignment(center);
		
		VBox set = new VBox();
		set.setAlignment(center);
		set.setSpacing(10);
	
		set.getChildren().add(nightmode);
		set.getChildren().add(defaultC);
		set.getChildren().add(colourSet);
		set.getChildren().add(deleteAccount);
		set.setSpacing(1);
		
		settings.setContent(set);
		
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String [] args) {
		launch(args);
	}
}
