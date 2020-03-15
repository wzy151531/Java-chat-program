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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.model.HomeModel;
import socotra.util.Util;

import java.util.ArrayList;
import java.util.TreeSet;

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
    @FXML
    private Label usernameLabel;
    @FXML
    private ListView<ChatSession> chatSessionList;
    @FXML
    private ListView<String> clientsList;
    @FXML
    private Label chatTitleLabel;
    @FXML
    private Button addGroupChatButton;
    @FXML
    private Button cancelAddingButton;
    @FXML
    private Button confirmAddingButton;
    /**
     * The emoji list, each list stores a length of 10 arrayList.
     */
    @FXML
    private ListView<ArrayList<String>> emojiList;

    private boolean isCreatingGroup;
    private TreeSet<String> newGroup = new TreeSet<>();

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

    public void refreshChatList() {
        Platform.runLater(() -> {
//            System.out.println("refresh chatList.");
            chatList.refresh();
        });
    }

    public void refreshChatSessionList() {
        Platform.runLater(() -> {
            chatSessionList.refresh();
        });
    }

    public void setChatListItems(ObservableList<ConnectionData> items) {
        Platform.runLater(() -> {
            chatList.setItems(items);
            scrollChatList();
        });
    }

    /**
     * Scroll chatList to the last row.
     */
    public void scrollChatList() {
        chatList.scrollTo(chatList.getItems().size() - 1);
    }

    /**
     * Initialize code when controller initializing.
     */
    @FXML
    private void initialize() {
        Client.setHomeModel(new HomeModel());
        usernameLabel.setText(Client.getClientThread().getUsername());
        stopButton.setDisable(true);
        sendAudioButton.setDisable(true);
        emojiList.setVisible(false);
        emojiList.setManaged(false);
        cancelAddingButton.setVisible(false);
        cancelAddingButton.setManaged(false);
        confirmAddingButton.setVisible(false);
        confirmAddingButton.setVisible(false);
        configEmojiList();
        configChatList();
        configChatSessionList();
        configClientsList();
        Platform.runLater(() -> { // runLater keep thread synchronize
//            chatList.setItems(Client.getHomeModel().getCertainChatData("all"));
            chatSessionList.setItems(Client.getHomeModel().getChatSessionList());
            clientsList.setItems(Client.getHomeModel().getClientsList());
        });
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
            @Override
            protected void updateItem(ConnectionData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else if (item.getType() == 2) {
                    Circle notSentCircle = new Circle(2.0, Color.RED);
                    Circle sentCircle = new Circle(2.0, Color.LIGHTGREEN);
                    Button button = new Button("Audio");
                    button.setPrefSize(50.0, 15.0);
                    button.setFont(new Font(10));
                    button.setOnAction(evt -> {
                        Client.getHomeModel().setForceStop(false);
                        captureButton.setDisable(true);
                        stopButton.setDisable(false);
                        allAudioButtons.forEach(n -> {
                            if (!n.equals(button)) {
                                n.setDisable(true);
                            }
                        });
                        Client.getHomeModel().handlePlayAudio(item.getAudioData());
                    });
                    if (item.getUserSignature().equals(Client.getClientThread().getUsername())) {
                        Label signature = new Label(" :" + item.getUserSignature());
                        HBox content = item.getIsSent() ? new HBox(sentCircle, button, signature) : new HBox(notSentCircle, button, signature);
                        content.setAlignment(Pos.CENTER_RIGHT);
                        setGraphic(content);
                    } else {
                        Label signature = new Label(item.getUserSignature() + ": ");
                        HBox content = new HBox(signature, button);
                        content.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(content);
                    }
                } else if (item.getType() == 1) {
                    // If user received own messages, show in right.
                    Circle notSentCircle = new Circle(2.0, Color.RED);
                    Circle sentCircle = new Circle(2.0, Color.LIGHTGREEN);
                    if (item.getUserSignature().equals(Client.getClientThread().getUsername())) {
                        Label signature = new Label(" :" + item.getUserSignature());
                        Label textData = new Label(item.getTextData());
                        HBox content = item.getIsSent() ? new HBox(sentCircle, textData, signature) : new HBox(notSentCircle, textData, signature);
                        content.setAlignment(Pos.CENTER_RIGHT);
                        setGraphic(content);

                    } else {
                        Label signature = new Label(item.getUserSignature() + ": ");
                        Label textData = new Label(item.getTextData());
                        HBox content = new HBox(signature, textData);
                        content.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(content);
                    }
                }
            }

        });
    }

    /**
     * Config chatSessionList render logic.
     */
    private void configChatSessionList() {
        chatSessionList.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(ChatSession item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else {
                    TreeSet<String> temp = new TreeSet<>(item.getToUsernames());
                    temp.remove(Client.getClientThread().getUsername());
                    String chatName = Util.generateChatName(temp);
                    Button button = new Button(chatName);
                    button.setPrefSize(170.0, 15.0);
                    button.setFont(new Font(15));
                    button.setOnAction(evt -> {
                        Client.getHomeModel().checkoutChatPanel(item);
                        chatTitleLabel.setText(chatName);
                        item.setHint(false);
                        chatSessionList.refresh();
                    });
                    Circle hintCircle = new Circle(2.0, Color.RED);
                    HBox content;
                    if (item.isHint()) {
//                        button.setStyle("-fx-base: #ee2211");
                        content = new HBox(hintCircle, button);
                    } else {
                        content = new HBox(button);
                    }
                    content.setPadding(Insets.EMPTY);
                    content.setAlignment(Pos.CENTER_RIGHT);
                    setGraphic(content);
                }
            }
        });
    }

    private void configClientsList() {
        clientsList.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else {

                    Button button = new Button("chat");
                    button.setPrefSize(50.0, 15.0);
                    button.setFont(new Font(15));
                    button.setOnAction(evt -> {
                        TreeSet<String> clients = new TreeSet<>();
                        clients.add(item);
                        if (!item.equals("all")) {
                            clients.add(Client.getClientThread().getUsername());
                        }
                        if (!Client.getHomeModel().chatSessionExist(clients)) {
                            ChatSession chatSession = new ChatSession(clients, true);
                            Client.getHomeModel().appendChatSessionList(chatSession);
                        }
                    });
                    Label clientName = new Label(item);
                    HBox content = new HBox();
                    content.setPrefWidth(180.0);
                    HBox content1 = new HBox(clientName);
                    content1.setAlignment(Pos.CENTER_LEFT);

                    HBox content2 = new HBox(button);
                    content2.setAlignment(Pos.CENTER_RIGHT);

                    if (isCreatingGroup && !item.equals("all")) {
                        CheckBox checkBox = new CheckBox();
                        checkBox.setOnAction(evt -> {
//                            System.out.println("item: " + checkBox.isSelected());
                            if (checkBox.isSelected()) {
                                newGroup.add(item);
                            } else {
                                newGroup.remove(item);
                            }
                        });
                        checkBox.setPrefWidth(content.getPrefWidth() * 0.2);
                        content1.setPrefWidth(content.getPrefWidth() * 0.4);
                        content2.setPrefWidth(content.getPrefWidth() * 0.4);
                        content.getChildren().add(checkBox);
                    } else {
                        content1.setPrefWidth(content.getPrefWidth() / 2);
                        content2.setPrefWidth(content.getPrefWidth() / 2);

                    }

                    content.getChildren().add(content1);
                    content.getChildren().add(content2);

                    setGraphic(content);
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

    @FXML
    public void addGroupChat(ActionEvent event) {
        isCreatingGroup = true;
        cancelAddingButton.setVisible(true);
        cancelAddingButton.setManaged(true);
        confirmAddingButton.setVisible(true);
        confirmAddingButton.setManaged(true);
        addGroupChatButton.setVisible(false);
        addGroupChatButton.setManaged(false);
        clientsList.refresh();
    }

    @FXML
    public void cancelAdding(ActionEvent event) {
        isCreatingGroup = false;
        cancelAddingButton.setVisible(false);
        cancelAddingButton.setManaged(false);
        confirmAddingButton.setVisible(false);
        confirmAddingButton.setManaged(false);
        addGroupChatButton.setVisible(true);
        addGroupChatButton.setManaged(true);
        clientsList.refresh();
    }

    @FXML
    public void confirmAdding(ActionEvent event) {
        System.out.println(this.newGroup);
        if (newGroup.size() <= 1) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Cannot create a group chat with less two people.", "Try again.").show();
            return;
        }
        newGroup.add(Client.getClientThread().getUsername());
        if (Client.getHomeModel().chatSessionExist(newGroup)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "The group chat has already existed.", "Try again.").show();
            return;
        }
        ChatSession chatSession = new ChatSession(newGroup, true);
        Client.getHomeModel().appendChatSessionList(chatSession);
        this.cancelAdding(event);
    }

}
