package socotra.service;

import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;
import socotra.util.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    /**
     * Start handle the communication with client.
     */
    public void run() {
        try {
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromClient.readObject();
                System.out.println("Received data.");
                switch (connectionData.getType()) {
                    // If connection data is about login.
                    case 0:
                        username = connectionData.getUsername();
                        String password = connectionData.getPassword();
                        try {
                            if (!JdbcUtil.validateUser(username, password)) {
                                System.out.println("Invalidated user.");
                                toClient.writeObject(new ConnectionData(false));
                                endClient();
                                return;
                            } else {
                                Server.addClient(username, toClient);
                                System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
                                Util.privateSend(new ConnectionData(true), username);

                                HashMap<ChatSession, List<ConnectionData>> chatData = JdbcUtil.getCertainChatData(username);
                                if (chatData != null) {
                                    Util.privateSend(new ConnectionData(chatData, "server"), username);
                                }

                                TreeSet<String> allClientsName = new TreeSet<>(Server.getClients().keySet());
                                allClientsName.remove(username);
                                // Inform the new client current online users and inform other clients that the new client is online.
                                Util.broadcast(new ConnectionData(username, true), username, new ConnectionData(allClientsName));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    // If connection data is about logout.
                    case -2:
                        Util.broadcast(new ConnectionData(username, false), username);
                        Server.removeClient(username);
                        System.out.println("User log out. Current online users: " + Server.getClients().keySet());
                        endClient();
                        return;
                    // If connection data is about received hint.
                    case -4:
                        Util.groupSend(connectionData, connectionData.getChatSession().getToUsernames());
                        break;
                    // If connection data is about normal chat message.
                    case 1:
                        connectionData.setIsSent(true);
                        // Once received the text data, use a new thread to insert it into database.
                        JdbcUtil.insertClientChatData(connectionData);
                    case 2:
                        connectionData.setIsSent(true);
                        // If want to given received hint once server receive connectionData.
                        if (connectionData.getChatSession().getToUsernames().size() == 1) {
                            Util.privateSend(new ConnectionData(connectionData.getUuid(), "server", connectionData.getChatSession()), connectionData.getUserSignature());
                            Util.broadcast(connectionData, connectionData.getUserSignature());
                        } else {
                            if (!Util.isAnyOnline(connectionData.getChatSession().getToUsernames())) {
                                Util.privateSend(new ConnectionData(connectionData.getUuid(), "server", connectionData.getChatSession()), connectionData.getUserSignature());
                            }
                            Util.groupSend(connectionData, connectionData.getChatSession().getToUsernames());
                        }
                        break;
                    // If connection data is about store chat history.
                    case 3:
                        JdbcUtil.updateClientsChatData(connectionData.getUserSignature(), connectionData.getChatData());
                        break;
                    default:
                        System.out.println("Unknown data.");
                }
            }
        } catch (IOException e) {
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
