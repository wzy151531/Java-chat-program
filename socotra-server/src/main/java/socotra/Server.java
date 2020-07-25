package socotra;

import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;
import socotra.service.ServerThread;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This file is entry of server, used to accept clients.
 */

public class Server {

    /**
     * All connected clients's username and their ObjectOutputStream.
     */
    private static HashMap<String, ObjectOutputStream> clients = new HashMap<>();
    /**
     * SSL server socket.
     */
    private static SSLServerSocket serverSocket;

    private static HashMap<String, ArrayList<ConnectionData>> depositPairwiseData = new HashMap<>();
    private static HashMap<String, ArrayList<ConnectionData>> depositSenderKeyData = new HashMap<>();
    private static HashMap<String, ArrayList<ConnectionData>> depositGroupData = new HashMap<>();

    /**
     * Initialize TLS before creating the SSL server socket.
     *
     * @throws Exception The Exception when initializing.
     */
    private static void initTLS() throws Exception {
        String SERVER_KEY_STORE = "src/main/resources/socotra_server_ks";
        String SERVER_KEY_STORE_PASSWORD = "socotra";
        System.setProperty("javax.net.ssl.trustStore", SERVER_KEY_STORE);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(SERVER_KEY_STORE), SERVER_KEY_STORE_PASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        context.init(kmf.getKeyManagers(), trustManagers, null);

        ServerSocketFactory factory = context.getServerSocketFactory();
        serverSocket = (SSLServerSocket) factory.createServerSocket(50443);
        serverSocket.setNeedClientAuth(false);
    }

    /**
     * Start server.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Open a server socket:
        try {
            initTLS();
            System.out.println("Server bound.");
            JdbcUtil.init();
            JdbcUtil.connect();
            // TODO
//            JdbcUtil.setClientsChatData(JdbcUtil.queryClientsChatData());
            System.out.println("Clients chat data loaded.");
//            Util.printClientsChatData(JdbcUtil.getClientsChatData());
        } catch (IOException e) {
            System.err.println("Couldn't listen on port: 50443.");
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listen to the socket, accepting connections from new clients, and running a new thread to serve each new client:
        try {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("Got a connection.");
                ServerThread s = new ServerThread(clientSocket);
                s.start();
            }
        } catch (Exception e) {
            System.out.println("Exception occurs.");
            e.printStackTrace();
            try {
                serverSocket.close();
            } catch (IOException io) {
                System.err.println("Couldn't close server socket" + io.getMessage());
            }
        } finally {
            try {
                JdbcUtil.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add new client to all clients hash map.
     *
     * @param username The username of client.
     * @param toClient The ObjectOutputStream of client.
     */
    public synchronized static void addClient(String username, ObjectOutputStream toClient) {
        Server.clients.put(username, toClient);
    }

    /**
     * Getter for all connected clients.
     *
     * @return All connected clients.
     */
    public synchronized static HashMap<String, ObjectOutputStream> getClients() {
        return Server.clients;
    }

    /**
     * Remove client from all connected clients use username and ObjectOutputStream.
     *
     * @param username The username of removed client.
     * @param toClient The ObjectOutputStream of removed client.
     */
    public synchronized static void removeClient(String username, ObjectOutputStream toClient) {
        Server.clients.remove(username, toClient);
    }

    /**
     * Remove client from all connected clients use username.
     *
     * @param username The username of removed client.
     */
    public synchronized static void removeClient(String username) {
        Server.clients.remove(username);
    }

    public synchronized static void storePairwiseData(String receiverName, ConnectionData pairwiseData) {
        System.out.println("Store pairwise data.");
        ChatSession chatSession = pairwiseData.getChatSession();
        if (pairwiseData.getType() != 7 && chatSession.getSessionType() != ChatSession.PAIRWISE) {
            throw new IllegalStateException("Bad pairwiseData.");
        }
        ArrayList<ConnectionData> des = Server.depositPairwiseData.getOrDefault(receiverName, new ArrayList<>());
        des.add(pairwiseData);
        Server.depositPairwiseData.put(receiverName, des);
    }

    public synchronized static void storeSenderKeyData(String receiverName, ConnectionData senderKeyData) {
        System.out.println("Store senderKey data.");
        if (senderKeyData.getType() != 8) {
            throw new IllegalStateException("Bad senderKeyData.");
        }
        ArrayList<ConnectionData> des = Server.depositSenderKeyData.getOrDefault(receiverName, new ArrayList<>());
        des.add(senderKeyData);
        Server.depositSenderKeyData.put(receiverName, des);
    }

    public synchronized static void storeGroupData(String receiverName, ConnectionData groupData) {
        System.out.println("Store group data.");
        ChatSession chatSession = groupData.getChatSession();
        if (groupData.getType() != 7 && chatSession.getSessionType() != ChatSession.GROUP) {
            throw new IllegalStateException("Bad groupData.");
        }
        ArrayList<ConnectionData> des = Server.depositGroupData.getOrDefault(receiverName, new ArrayList<>());
        des.add(groupData);
        Server.depositGroupData.put(receiverName, des);
    }

    public synchronized static ArrayList<ConnectionData> loadPairwiseData(String username) {
        ArrayList<ConnectionData> result = Server.depositPairwiseData.get(username);
        Server.depositPairwiseData.remove(username);
        return result;
    }

    public synchronized static ArrayList<ConnectionData> loadSenderKeyData(String username) {
        ArrayList<ConnectionData> result = Server.depositSenderKeyData.get(username);
        Server.depositSenderKeyData.remove(username);
        return result;
    }

    public synchronized static ArrayList<ConnectionData> loadGroupData(String username) {
        ArrayList<ConnectionData> result = Server.depositGroupData.get(username);
        Server.depositGroupData.remove(username);
        return result;
    }

}
