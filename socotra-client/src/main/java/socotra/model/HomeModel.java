package socotra.model;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.controller.HomeController;
import socotra.util.SendThread;
import socotra.util.Util;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

/**
 * This file is about the data model in home page.
 */

public class HomeModel {

    /**
     * Local history message.
     */
    private HashMap<ChatSession, ObservableList<ConnectionData>> chatData = new HashMap<>();
    /**
     * Current chosen chat session.
     */
    private ChatSession currentChatSession;
    /**
     * Created chat sessions.
     */
    private ObservableList<ChatSession> chatSessionList = FXCollections.observableArrayList(new ArrayList<>());
    /**
     * Current online clients list.
     */
    private ObservableList<String> clientsList = FXCollections.observableArrayList(new ArrayList<>());
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

    /**
     * Constructor for HomeModel.
     */
    public HomeModel() {
        audioFormat = getAudioFormat();
    }

    /**
     * Set chat history data from server to chatData.
     *
     * @param chatData The chat history data from server.
     */
    public void setChatData(HashMap<ChatSession, List<ConnectionData>> chatData) {
        Platform.runLater(() -> {
            chatData.forEach((k, v) -> {
                this.chatData.put(k, FXCollections.observableArrayList(v));
                chatSessionList.add(k);
            });
        });
    }

    /**
     * Getter for currentChatSession.
     *
     * @return Current chosen chat session.
     */
    public ChatSession getCurrentChatSession() {
        return this.currentChatSession;
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
     * Get certain chat data according to the given chat session.
     *
     * @param chatSession The given chat session.
     * @return The certain data of chat session.
     */
    public ObservableList<ConnectionData> getCertainChatData(ChatSession chatSession) {
        ObservableList<ConnectionData> certainChatData = this.chatData.get(chatSession);
        if (certainChatData == null) {
            certainChatData = FXCollections.observableArrayList(new ArrayList<>());
            this.chatData.put(chatSession, certainChatData);
        }
        return certainChatData;
    }

    /**
     * Getter for chat session list.
     *
     * @return Created chat session list.
     */
    public ObservableList<ChatSession> getChatSessionList() {
        return this.chatSessionList;
    }

    /**
     * Getter for online clients list.
     *
     * @return Current online clients.
     */
    public ObservableList<String> getClientsList() {
        return this.clientsList;
    }

    /**
     * Append new connectionData to historyData.
     *
     * @param connectionData New connectionData.
     */
    public void appendChatData(ConnectionData connectionData) {
        Platform.runLater(() -> {
            ChatSession key = connectionData.getChatSession();

            ObservableList<ConnectionData> certainChatData = this.chatData.get(key);
            if (certainChatData == null) {
                certainChatData = FXCollections.observableArrayList(new ArrayList<>());
                certainChatData.add(connectionData);
                this.chatData.put(key, certainChatData);
                this.appendChatSessionList(key);
            } else {
                certainChatData.add(connectionData);
            }
            Client.getHomeController().scrollChatList();
            if (currentChatSession == null || !key.equals(currentChatSession)) {
                System.out.println("need to set hint");
                System.out.println(chatSessionList);
                System.out.println(key);
                chatSessionList.forEach(n -> {
                    if (n.equals(key)) {
                        System.out.println("Set hint true");
                        n.setHint(true);
                        Client.getHomeController().refreshChatSessionList();
                    }
                });
            }
        });
    }

    /**
     * Judge if chat session has existed in chat session list.
     *
     * @param clients The chat session needs to be created.
     * @return The boolean indicates whether the new chat session has existed in chat session list.
     */
    public boolean chatSessionExist(TreeSet<String> clients) {
        for (ChatSession chatSession : chatSessionList) {
            if (chatSession.getToUsernames().equals(clients)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update chat data's read status.
     *
     * @param uuid        The connectionData's uuid.
     * @param chatSession The chat session that connectionData belongs to.
     */
    public void updateChatData(UUID uuid, ChatSession chatSession) {
        Platform.runLater(() -> {
            this.chatData.get(chatSession).forEach(n -> {
                if (uuid.equals(n.getUuid()) && !n.getIsSent()) {
                    n.setIsSent(true);
                    Client.getHomeController().refreshChatList();
                }
            });
        });
    }

    /**
     * Append new chat session to chat session list.
     *
     * @param chatSession New chat session.
     */
    public void appendChatSessionList(ChatSession chatSession) {
        Platform.runLater(() -> {
            chatSession.setHint(true);
            this.chatSessionList.add(chatSession);
        });
    }

    /**
     * Remove chat session from chat session list.
     *
     * @param chatSession The chat session needs to be removed.
     */
    public void removeChatSessionList(ChatSession chatSession) {
        Platform.runLater(() -> {
            this.chatSessionList.remove(chatSession);
        });
    }

    /**
     * Append new client to clients list.
     *
     * @param clientName New client.
     */
    public void appendClientsList(String clientName) {
        Platform.runLater(() -> {
            this.clientsList.add(clientName);
        });
    }

    /**
     * Remove clinet from clients list.
     *
     * @param clientName The client needs to be removed.
     */
    public void removeClientsList(String clientName) {
        Platform.runLater(() -> {
            this.clientsList.remove(clientName);
        });
    }

    /**
     * Checkout chat session panel.
     *
     * @param chatSession The new chat session checkout to.
     */
    public void checkoutChatPanel(ChatSession chatSession) {
        this.currentChatSession = chatSession;
        Client.getHomeController().setChatListItems(getCertainChatData(chatSession));
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
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Cannot send empty message.", "Try again.").show();
        } else if (this.currentChatSession == null) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Please choose a user to send message.", "Try again.").show();
        } else {
            ConnectionData connectionData = new ConnectionData(text, Client.getClientThread().getUsername(), currentChatSession);
            appendChatData(connectionData);
            new SendThread(connectionData).start();
        }
    }

    /**
     * Send audio to the server.
     */
    public void handleSendAudio() {
        try {
            byte[] audioData = byteArrayOutputStream.toByteArray();
            ConnectionData connectionData = new ConnectionData(audioData, Client.getClientThread().getUsername(), currentChatSession);
            appendChatData(connectionData);
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
     * Send log out connectionData to server.
     */
    public void handleLogout() {
        HashMap<ChatSession, List<ConnectionData>> newChatData = new HashMap<>();
        chatData.forEach((k, v) -> {
            List<ConnectionData> connectionDataList = new ArrayList<>(v);
            newChatData.put(k, connectionDataList);
        });
        ConnectionData connectionData1 = new ConnectionData(newChatData, Client.getClientThread().getUsername());
        new SendThread(connectionData1, true).start();
    }

    /**
     * The send thread to run the send either audio or text message job.
     */

    /**
     * The search chatdata function
     *
     * @return a list of chatdata with search characters inside.
     */
    public ArrayList<ConnectionData> search(String input) {
        List<ConnectionData> chatList = chatData.get(currentChatSession);
        ArrayList<ConnectionData> searchList = new ArrayList<ConnectionData>();

        for (ConnectionData cd : chatList) {
            if (cd.getType() == 1 && cd.getTextData().contains(input)) {
                searchList.add(cd);
            }
        }
        return searchList;
    }

    /**
     * The go back chat function
     *
     * @return a list of all chatdata.
     */
    public ArrayList<ConnectionData> oldChat() {
        List<ConnectionData> chatList = chatData.get(currentChatSession);
        ArrayList<ConnectionData> oldChatList = new ArrayList<>();

        for (ConnectionData cd : chatList) {
            if (cd.getType() == 1) {
                oldChatList.add(cd);
            }
        }
        return oldChatList;
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
        byte[] tempBuffer = new byte[10000];
        // TODO forceStop
//        byte[] tempBuffer = new byte[10];

        public void run() {
            try {
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();

                int cnt;
                // Keep looping until the input read method returns -1 for empty stream.
                // TODO: forceStop
//                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !forceStop) {
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
