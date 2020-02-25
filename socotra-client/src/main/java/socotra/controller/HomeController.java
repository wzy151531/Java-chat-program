package socotra.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import socotra.common.ConnectionData;
import socotra.service.ClientThread;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class HomeController {

    private ConnectionData connectionData;
    @FXML
    private Button captureButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button playButton;
    @FXML
    private Button sendAudioButton;
    @FXML
    private Button sendTextButton;
    @FXML
    private TextField messageField;
    @FXML
    private Text messageText;
    private boolean stopCapture = false;
    private ByteArrayOutputStream byteArrayOutputStream;
    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private AudioInputStream audioInputStream;
    private SourceDataLine sourceDataLine;
    private ClientThread clientThread;

    public void setConnectionData(ConnectionData connectionData) {
        this.connectionData = connectionData;
        if (connectionData.getType() == 2) {
            playButton.setDisable(false);
            messageText.setText("Got a audio from " + connectionData.getUserSignature());
        } else if (connectionData.getType() == 1) {
            playButton.setDisable(true);
            messageText.setText("Got: " + connectionData.getTextData() + "  from " + connectionData.getUserSignature());
        }
    }

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    @FXML
    private void initialize() {
        stopButton.setDisable(true);
        playButton.setDisable(true);
        sendAudioButton.setDisable(true);
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
                connectionData = new ConnectionData(messageField.getText(), clientThread.getUsername());
                new SendThread().start();
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
        playButton.setDisable(true);
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
    }

    @FXML
    public void play(ActionEvent event) {
        captureButton.setDisable(true);
        stopButton.setDisable(false);
        playButton.setDisable(true);
        sendAudioButton.setDisable(true);
        try {
            // Get everything set up for playback.
            // Get the previously-saved data into a byte array object.
            // Get an input stream on the byte array containing the data
            byte[] audioData = connectionData.getAudioData();
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
            connectionData = new ConnectionData(audioData, clientThread.getUsername());
            new SendThread().start();
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
        byte[] tempBuffer = new byte[10000];

        public void run() {
            try {
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                int cnt;
                // Keep looping until the input read method returns -1 for empty stream.
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of the data line where it will be delivered
                        // to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }
                }
                // Block and wait for internal buffer of the data line to empty.
                sourceDataLine.drain();
                sourceDataLine.close();
                playButton.setDisable(false);
                stopButton.setDisable(true);
                captureButton.setDisable(false);
                sendAudioButton.setDisable(false);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                System.exit(0);
            }
        }
    }

    class SendThread extends Thread {
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
