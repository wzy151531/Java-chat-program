package socotra.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import socotra.Client;
import socotra.common.ConnectionData;
import socotra.model.HomeModel;

import java.util.ArrayList;

public class HomeController {

    /**
     * All audio button in local history message.
     */
    private ArrayList<Button> allAudioButtons = new ArrayList<>();
    @FXML
    private Button captureButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button sendAudioButton;
    @FXML
    private Button sendTextButton;
    @FXML
    private Button logoutButton;
    @FXML
    private TextField messageField;
    @FXML
    private ListView<ConnectionData> chatList;
    @FXML
    private Button emojiButton;
    /**
     * The emoji list, each list stores a length of 10 arrayList.
     */
    @FXML
    private ListView<ArrayList<String>> emojiList;

    public void setStopButtonDisabled(boolean disabled) {
        this.stopButton.setDisable(disabled);
    }

    public void setCaptureButtonDisabled(boolean disabled) {
        this.captureButton.setDisable(disabled);
    }

    public void setSendAudioButtonDisabled(boolean disabled) {
        this.sendAudioButton.setDisable(disabled);
    }

    public void setAllAudioButtonsDisabled(boolean disabled) {
        this.allAudioButtons.forEach(n -> {
            n.setDisable(disabled);
        });
    }

    /**
     * Initialize code when controller initializing.
     */
    @FXML
    private void initialize() {
        Client.setHomeModel(new HomeModel());
        stopButton.setDisable(true);
        sendAudioButton.setDisable(true);
        emojiList.setVisible(false);
        emojiList.setManaged(false);
        configEmojiList();
        configChatList();
        // Generate emojiList view according to the render logic of it.
        generateEmojiListView();
    }

    /**
     * Config the emojiList's render logic.
     */
    private void configEmojiList() {
        // Define the render logic of the emojiList.
        emojiList.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(ArrayList<String> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else {
                    HBox row = new HBox();
                    // Add each emoji in one line into the HBox.
                    item.forEach(n -> {
                        Button button = new Button(n);
                        button.setPrefSize(40.0, 20.0);
                        button.setPadding(Insets.EMPTY);
                        button.setTextAlignment(TextAlignment.CENTER);
                        button.setFont(new Font("NotoEmoji-Regular", 25));
                        button.setOnAction(evt -> {
                            messageField.setText(messageField.getText() + button.getText());
                        });
                        row.getChildren().add(button);
                    });
                    setGraphic(row);
                }
            }
        });
    }

    /**
     * Config the chatList's render logic.
     */
    private void configChatList() {
        chatList.setCellFactory(l -> new ListCell<>() {

            private final Button button = new Button("play");

            {
                button.setPrefSize(50.0, 15.0);
                button.setFont(new Font(10));
                allAudioButtons.add(button);
                button.setOnAction(evt -> {
                    Client.getHomeModel().setForceStop(false);
                    captureButton.setDisable(true);
                    stopButton.setDisable(false);
                    allAudioButtons.forEach(n -> {
                        if (!n.equals(button)) {
                            n.setDisable(true);
                        }
                    });
                    ConnectionData item = getItem();
                    if (item.getType() == 2) {
                        Client.getHomeModel().handlePlayAudio(item.getAudioData());
                    }
                });
            }

            @Override
            protected void updateItem(ConnectionData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else if (item.getType() == 2) {
                    if (item.getUserSignature().equals(Client.getClientThread().getUsername())) {
                        Label signature = new Label(" :" + item.getUserSignature());
                        HBox content = new HBox(button, signature);
                        content.setAlignment(Pos.CENTER_RIGHT);
                        setGraphic(content);
                    } else {
                        Label signature = new Label(item.getUserSignature() + ": ");
                        HBox content = new HBox(signature, button);
                        setGraphic(content);
                    }
                } else if (item.getType() == 1) {
                    // If user received own messages, show in right.
                    if (item.getUserSignature().equals(Client.getClientThread().getUsername())) {
                        setText(item.getTextData() + " :" + item.getUserSignature());
                        setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        setText(item.getUserSignature() + ": " + item.getTextData());
                    }
                }
            }

        });
    }

    /**
     * Generate emojiList view according to the render logic of it.
     */
    private void generateEmojiListView() {
        ArrayList<ArrayList<String>> emojiData = Client.getHomeModel().generateEmojiList();
        ObservableList<ArrayList<String>> dataList = FXCollections.observableArrayList(emojiData);
        // 'runLater' keep thread synchronize.
        Platform.runLater(() -> {
            emojiList.setItems(null);
            emojiList.setItems(dataList);
            emojiList.refresh();
            emojiList.scrollTo(0);
        });
    }

    /**
     * Update the ChatListView in home page.
     */
    public void updateChatView() {
        ObservableList<ConnectionData> dataList = FXCollections.observableArrayList(Client.getHomeModel().getHistoryData());
        Platform.runLater(() -> { // runLater keep thread synchronize
            chatList.setItems(null);
            chatList.setItems(dataList);
            chatList.refresh();
            chatList.scrollTo(chatList.getItems().size() - 1);
        });
    }


    /**
     * Send text message to server.
     *
     * @param event The event ot sendTextButton.
     */
    @FXML
    public void sendText(ActionEvent event) {
        Client.getHomeModel().handleSendText(messageField.getText());
        messageField.setText("");
    }

    /**
     * Capture audio input from a microphone and save it in a ByteArrayOutputStream object.
     *
     * @param event The capture event of captureButton.
     */
    @FXML
    public void capture(ActionEvent event) {
        captureButton.setDisable(true);
        stopButton.setDisable(false);
        sendAudioButton.setDisable(true);
        Client.getHomeModel().handleCapture();
    }

    /**
     * Stop capturing the audio.
     *
     * @param event The stop event of the stopButton.
     */
    @FXML
    public void stop(ActionEvent event) {
        captureButton.setDisable(false);
        stopButton.setDisable(true);
        sendAudioButton.setDisable(false);
        Client.getHomeModel().handleStop();
    }

    /**
     * Show emoji pane.
     *
     * @param event The event of emojiButton.
     */
    @FXML
    public void showEmojiList(ActionEvent event) {
        emojiList.setVisible(!emojiList.isVisible());
        emojiList.setManaged(!emojiList.isManaged());
    }


    /**
     * Send audio data to the server.
     *
     * @param event The event of the sendAudioButton.
     */
    @FXML
    public void sendAudio(ActionEvent event) {
        Client.getHomeModel().handleSendAudio();
    }

    @FXML
    public void logout(ActionEvent event) {
        Client.getHomeModel().handleLogout();
    }

}
