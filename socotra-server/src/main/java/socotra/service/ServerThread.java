package socotra.service;

import jdk.jshell.spi.ExecutionControlProvider;
import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;
import socotra.util.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * This file is used to communicate with clients.
 */

public class ServerThread extends Thread {

    /**
     * Client socket accepted in.
     */
    private Socket client;
    /**
     * ObjectOutputStream to client socket.
     */
    private ObjectOutputStream toClient;
    /**
     * The client's username.
     */
    private String username;
    /**
     * ObjectInputStream from client socket.
     */
    private ObjectInputStream fromClient;

    /**
     * Constructor of ServerThread.
     *
     * @param client The client socket that server thread communicates with.
     * @throws Exception The IOException.
     */
    public ServerThread(Socket client) throws Exception {
        super("ServerThread");
        this.client = client;
        this.toClient = new ObjectOutputStream(client.getOutputStream());
        this.fromClient = new ObjectInputStream(client.getInputStream());
    }

    /**
     * Do something when client needs to disconnect.
     *
     * @throws IOException The IOException when closing the stream.
     */
    private void endClient() throws IOException {
        toClient.close();
        fromClient.close();
        client.close();
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void inform(ConnectionData connectionData) throws IOException {
        toClient.writeObject(connectionData);
    }

    void appendClient() {
        Server.addClient(username, toClient);
    }

    boolean processSignUp(ConnectionData connectionData) {
        try {
            int userId = JdbcUtil.signUp(connectionData.getUsername(), connectionData.getPassword());
            // TODO
            JdbcUtil.storeKeyBundle(userId, connectionData.getKeyBundle());
            toClient.writeObject(new ConnectionData(-5, true));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            try {
                toClient.writeObject(new ConnectionData(-5, false));
                return false;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Start handle the communication with client.
     */
    public void run() {
        try {
            DataHandler dataHandler = new DataHandler(this);
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromClient.readObject();
                System.out.println("Received data.");
                if (!dataHandler.handle(connectionData)) {
                    return;
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Something went wrong. Ending service to client...");
            Server.removeClient(username, toClient);
            System.out.println("User removed. Current online users: " + Server.getClients().keySet());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                endClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
