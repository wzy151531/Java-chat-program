package socotra.controller;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
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
import socotra.common.ConnectionData;
import socotra.model.ClientThread;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HomeController {

    /**
     * Local history message.
     */
    private ArrayList<ConnectionData> historyData = new ArrayList<>();
    /**
     * All audio button in local history message.
     */
    private ArrayList<Button> allAudioButton = new ArrayList<>();
    /**
     * The force stop flag.
     */
    private boolean forceStop = false;
    @FXML
    private Button captureButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button sendAudioButton;
    @FXML
    private Button sendTextButton;
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
    /**
     * The stop capture flag.
     */
    private boolean stopCapture = false;
    /**
     * Captures audio input from a microphone and saves it in a this object to output.
     */
    private ByteArrayOutputStream byteArrayOutputStream;
    /**
     * Capture audio input in this data line.
     */
    private TargetDataLine targetDataLine;
    /**
     * An AudioFormat object for a given set of format parameters.
     */
    private AudioFormat audioFormat;
    /**
     * Get an input stream on the byte array containing the data.
     */
    private AudioInputStream audioInputStream;
    /**
     * The data line where it will be delivered to the speaker.
     */
    private SourceDataLine sourceDataLine;
    /**
     * Injected clientThread to get some information of user.
     */
    private ClientThread clientThread;

    /**
     * Initialize code when controller initializing.
     */
    @FXML
    private void initialize() {
        stopButton.setDisable(true);
        sendAudioButton.setDisable(true);
        emojiList.setVisible(false);
        emojiList.setManaged(false);
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
        // Generate emojiList view according to the render logic of it.
        generateEmojiListView();
    }

    /**
     * Setter for clientThread to set received connection data.
     *
     * @param connectionData The connection data set by clientThread.
     */
    public void setConnectionData(ConnectionData connectionData) {
        this.historyData.add(connectionData);
        updateChatView();
    }

    /**
     * Generate emojiList data of all the emoji.
     *
     * @param emojiData All the emoji got by EmojiManager.
     * @return
     */
    private ArrayList<ArrayList<String>> generateEmojiList(Collection<Emoji> emojiData) {
        List<Emoji> emojiDataList = (List<Emoji>) emojiData;
        List<Emoji> filteredEmojiDataList = new ArrayList<>();
        // Filter the emoji from 'smile' to 'regional_indicator_symbol_z' because of the wrong display.
        boolean start = false;
        for (Emoji e : emojiDataList) {
            if (e.getAliases().contains("smile")) {
                start = true;
            }
            if (e.getAliases().contains("regional_indicator_symbol_z")) {
                start = false;
            }
            if (start) {
                filteredEmojiDataList.add(e);
            }
        }
        // Add the emoji's unicode to the ArrayList by 10.
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (int i = 0; i < filteredEmojiDataList.size(); i = i + 10) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                if (i + j >= filteredEmojiDataList.size()) continue;
                row.add(filteredEmojiDataList.get(i + j).getUnicode());
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Generate emojiList view according to the render logic of it.
     */
    private void generateEmojiListView() {
        ArrayList<ArrayList<String>> emojiData = generateEmojiList(EmojiManager.getAll());
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
    private void updateChatView() {
        chatList.setCellFactory(l -> new ListCell<>() {

            private final Button button = new Button("play");

            {
                button.setPrefSize(50.0, 15.0);
                button.setFont(new Font(10));
                allAudioButton.add(button);
                button.setOnAction(evt -> {
                    forceStop = false;
                    captureButton.setDisable(true);
                    stopButton.setDisable(false);
                    allAudioButton.forEach(n -> {
                        if (!n.equals(button)) {
                            n.setDisable(true);
                        }
                    });
                    ConnectionData item = getItem();
                    if (item.getType() == 2) {
                        playAudio(item.getAudioData());
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
                    if (item.getUserSignature().equals(clientThread.getUsername())) {
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
                    if (item.getUserSignature().equals(clientThread.getUsername())) {
                        setText(item.getTextData() + " :" + item.getUserSignature());
                        setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        setText(item.getUserSignature() + ": " + item.getTextData());
                    }
                }
            }

        });

        ObservableList<ConnectionData> dataList = FXCollections.observableArrayList(historyData);
        Platform.runLater(() -> { // runLater keep thread synchronize
            chatList.setItems(null);
            chatList.setItems(dataList);
            chatList.refresh();
            chatList.scrollTo(chatList.getItems().size() - 1);
        });
    }

    /**
     * Setter for clientThread to let clientThread inject itself.
     *
     * @param clientThread Injected clientThread for get some user information.
     */
    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    /**
     * Creates an AudioFormat object for a given set of format parameters.
     *
     * @return An AudioFormat object for a given set of format parameters.
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        // 8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        // 8,16
        int channels = 1;
        // 1,2
        boolean signed = true;
        // true,false
        boolean bigEndian = false;
        // true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Send text message to server.
     *
     * @param event The event ot sendTextButton.
     */
    @FXML
    public void sendText(ActionEvent event) {
        if (isEmpty(messageField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot send empty message.");
            alert.setContentText("Try again.");
            alert.show();
        } else {
            ConnectionData connectionData = new ConnectionData(messageField.getText(), clientThread.getUsername());
            new SendThread(connectionData).start();
            messageField.setText("");
        }
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
        try {
            // Get everything set up for capture.
            AudioFormat audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

            // Create a thread to capture the microphone data and start it running. It will run until the stopButton is clicked.
            new CaptureThread().start();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
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
        stopCapture = true;
        targetDataLine.stop();
        targetDataLine.close();
        forceStop = true;
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
     * Play audio in history data.
     *
     * @param audioData The audio needs to be played.
     */
    private void playAudio(byte[] audioData) {
        try {
            // Get everything set up for playback.
            // Get the previously-saved data into a byte array object.
            //
            InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
                    audioData.length / audioFormat.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            // Create a thread to play back the data and start it running. It will run until
            // all the data has been played back.
            new PlayThread().start();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    /**
     * Send audio data to the server.
     *
     * @param event The event of the sendAudioButton.
     */
    @FXML
    public void sendAudio(ActionEvent event) {
        try {
            byte[] audioData = byteArrayOutputStream.toByteArray();
            ConnectionData connectionData = new ConnectionData(audioData, clientThread.getUsername());
            new SendThread(connectionData).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the String is empty.
     *
     * @param str The given String.
     * @return If the String is empty, return true; else return false.
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * The capture thread to run the capture audio job.
     */
    class CaptureThread extends Thread {
        // An arbitrary-size temporary holding buffer
        byte[] tempBuffer = new byte[10000];

        public void run() {
            byteArrayOutputStream = new ByteArrayOutputStream();
            stopCapture = false;
            try {
                targetDataLine.open();
                targetDataLine.start();
                while (!stopCapture) {
                    // Read data from the internal buffer of the data line.
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        // Save data in output stream object.
                        byteArrayOutputStream.write(tempBuffer, 0, cnt);
                    }
                }
                byteArrayOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                System.exit(0);
            }
        }
    }

    /**
     * The play thread to run the play audio job.
     */
    class PlayThread extends Thread {
        //byte[] tempBuffer = new byte[10000];
        byte[] tempBuffer = new byte[10];

        public void run() {
            try {
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                int cnt;
                // Keep looping until the input read method returns -1 for empty stream.
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !forceStop) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of the data line where it will be delivered
                        // to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }
                }
                // Block and wait for internal buffer of the data line to empty.
                sourceDataLine.drain();
                sourceDataLine.close();
                stopButton.setDisable(true);
                captureButton.setDisable(false);
                sendAudioButton.setDisable(false);
                allAudioButton.forEach(n -> {
                    n.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace(System.out);
                System.exit(0);
            }
        }
    }

    /**
     * The send thread to run the send either audio or text message job.
     */
    class SendThread extends Thread {
        private ConnectionData connectionData;

        public SendThread(ConnectionData connectionData) {
            this.connectionData = connectionData;
        }

        public void run() {
            try {
                ObjectOutputStream toServer = clientThread.getToServer();
                toServer.writeObject(connectionData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
