package socotra.controller;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import socotra.common.ConnectionData;
import socotra.service.ClientThread;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HomeController {

    private ArrayList<ConnectionData> historyData = new ArrayList<>();
    private ArrayList<Button> allAudioButton = new ArrayList<>();
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
    @FXML
    private ListView<ArrayList<String>> emojiList;

    private boolean stopCapture = false;
    private ByteArrayOutputStream byteArrayOutputStream;
    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private AudioInputStream audioInputStream;
    private SourceDataLine sourceDataLine;
    private ClientThread clientThread;

    @FXML
    private void initialize() {
        stopButton.setDisable(true);
        sendAudioButton.setDisable(true);
        emojiList.setVisible(false);
        emojiList.setManaged(false);
        emojiList.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(ArrayList<String> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText("");
                } else {
                    HBox row = new HBox();
                    item.forEach(n -> {
                        Button button = new Button(n);
                        button.setPrefSize(40.0, 20.0);
                        button.setPadding(Insets.EMPTY);
                        button.setTextAlignment(TextAlignment.CENTER);
                        button.setFont(new Font("NotoEmoji-Regular", 25));
                        button.setOnAction(evt -> {
                            System.out.println(button.getText());
                            messageField.setText(messageField.getText() + " " + button.getText());
                        });
                        row.getChildren().add(button);
                    });
                    setGraphic(row);
                }
            }
        });
        generateEmojiListView();
    }

    public void setConnectionData(ConnectionData connectionData) {
        this.historyData.add(connectionData);
        updateChatView();
    }

    private ArrayList<ArrayList<String>> generateEmojiList(Collection<Emoji> emojiData) {
        List<Emoji> emojiDataList = (List<Emoji>) emojiData;
        List<Emoji> filteredEmojiDataList = new ArrayList<>();
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

    private void generateEmojiListView() {
        ArrayList<ArrayList<String>> emojiData = generateEmojiList(EmojiManager.getAll());
        ObservableList<ArrayList<String>> dataList = FXCollections.observableArrayList(emojiData);
        Platform.runLater(() -> { // runLater keep thread synchronize
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
                    Label signature = new Label(item.getUserSignature() + ": ");
                    HBox content = new HBox(signature, button);
                    setGraphic(content);
                } else if (item.getType() == 1) {
                    setText(item.getUserSignature() + ": " + item.getTextData());
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

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    // This method creates and returns an AudioFormat object for a given set of
    // format parameters. If these
    // parameters don't work well for you, try some of the other allowable parameter
    // values, which
    // are shown in comments following the declarations.
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

    @FXML
    public void sendText(ActionEvent event) {
        if (isEmpty(messageField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unexpected Error.");
            alert.setContentText("Try again.");
            alert.show();
        } else {
            try {
                ConnectionData connectionData = new ConnectionData(messageField.getText(), clientThread.getUsername());
                new SendThread(connectionData).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    // This method captures audio input from a microphone and saves it in a ByteArrayOutputStream object.
    public void capture(ActionEvent event) {
        captureButton.setDisable(true);
        stopButton.setDisable(false);
        sendAudioButton.setDisable(true);
        try {
            // Get everything set up for capture
            AudioFormat audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

            // Create a thread to capture the microphone data and start it running. It will
            // run until the Stop button is clicked.
            new CaptureThread().start();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

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

    @FXML
    public void showEmojiList(ActionEvent event) {
        emojiList.setVisible(!emojiList.isVisible());
        emojiList.setManaged(!emojiList.isManaged());
    }

    private void playAudio(byte[] audioData) {
        try {
            // Get everything set up for playback.
            // Get the previously-saved data into a byte array object.
            // Get an input stream on the byte array containing the data
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
