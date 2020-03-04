package socotra.model;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.scene.control.Alert;
import socotra.Client;
import socotra.common.ConnectionData;
import socotra.controller.HomeController;
import socotra.util.Util;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HomeModel {

    /**
     * Local history message.
     */
    private ArrayList<ConnectionData> historyData = new ArrayList<>();
    /**
     * An AudioFormat object for a given set of format parameters.
     */
    private AudioFormat audioFormat;
    /**
     * The stop capture flag.
     */
    private boolean stopCapture = false;
    /**
     * The force stop flag that indicates whether to force stop the playing audio process.
     */
    private boolean forceStop = false;
    /**
     * Captures audio input from a microphone and saves it in a this object to output.
     */
    private ByteArrayOutputStream byteArrayOutputStream;
    /**
     * Capture audio input in this data line.
     */
    private TargetDataLine targetDataLine;
    /**
     * Get an input stream on the byte array containing the data.
     */
    private AudioInputStream audioInputStream;
    /**
     * The data line where it will be delivered to the speaker.
     */
    private SourceDataLine sourceDataLine;

    public HomeModel() {
        audioFormat = getAudioFormat();
    }

    /**
     * Setter for forceStop.
     *
     * @param forceStop The force stop flag that indicates whether to force stop the playing audio process.
     */
    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
    }

    /**
     * Getter for historyData.
     *
     * @return Local history message.
     */
    public ArrayList<ConnectionData> getHistoryData() {
        return this.historyData;
    }

    /**
     * Append new connectionData to historyData.
     *
     * @param connectionData New connectionData.
     */
    public void appendHistoryData(ConnectionData connectionData) {
        this.historyData.add(connectionData);
        Client.getHomeController().updateChatView();
    }

    /**
     * Generate emojiList data of all the emoji.
     *
     * @return Formatted emojiData.
     */
    public ArrayList<ArrayList<String>> generateEmojiList() {
        List<Emoji> emojiDataList = (List<Emoji>) EmojiManager.getAll();
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
     * Creates an AudioFormat object for a given set of format parameters.
     *
     * @return An AudioFormat object for a given set of format parameters.
     */
    public AudioFormat getAudioFormat() {
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
     * Send text to the server.
     *
     * @param text The text needs to be sent.
     */
    public void handleSendText(String text) {
        if (Util.isEmpty(text)) {
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "Cannot send empty message.", "Try again.").show();
        } else {
            ConnectionData connectionData = new ConnectionData(text, Client.getClientThread().getUsername());
            new SendThread(connectionData).start();
        }
    }

    /**
     * Send audio to the server.
     */
    public void handleSendAudio() {
        try {
            byte[] audioData = byteArrayOutputStream.toByteArray();
            ConnectionData connectionData = new ConnectionData(audioData, Client.getClientThread().getUsername());
            new SendThread(connectionData).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop capture the audio.
     */
    public void handleStop() {
        stopCapture = true;
        targetDataLine.stop();
        targetDataLine.close();
        forceStop = true;
    }

    /**
     * Play audio in history data.
     *
     * @param audioData The audio needs to be played.
     */
    public void handlePlayAudio(byte[] audioData) {
        try {
            // Get everything set up for playback.
            // Get the previously-saved data into a byte array object.
            //
            InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
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
     * Capture the audio.
     */
    public void handleCapture() {
        try {
            // Get everything set up for capture.
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
     * The send thread to run the send either audio or text message job.
     */
    class SendThread extends Thread {
        private ConnectionData connectionData;

        private SendThread(ConnectionData connectionData) {
            this.connectionData = connectionData;
        }

        public void run() {
            try {
                ObjectOutputStream toServer = Client.getClientThread().getToServer();
                toServer.writeObject(connectionData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                HomeController homeController = Client.getHomeController();
                homeController.setStopButtonDisabled(true);
                homeController.setCaptureButtonDisabled(false);
                homeController.setSendAudioButtonDisabled(false);
                homeController.setAllAudioButtonsDisabled(false);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                System.exit(0);
            }
        }
    }

}