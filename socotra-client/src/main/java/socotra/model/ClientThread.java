package socotra.model;

import socotra.Client;
import socotra.common.ConnectionData;
import socotra.util.SendThread;
import socotra.util.SetChatData;
import socotra.util.SetOnlineUsers;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;

/**
 * This thread is used to communicate with server.
 */

public class ClientThread extends Thread {

    /**
     * The controller of login page.
     */
    private final LoginModel loginModel;
    /**
     * Current connected server name.
     */
    private final String serverName;
    /**
     * Current user's username.
     */
    private final String username;
    /**
     * Current user's password.
     */
    private final String password;
    /**
     * The output stream of connection.
     */
    private ObjectOutputStream toServer;
    /**
     * The input stream of connection.
     */
    private ObjectInputStream fromServer;
    /**
     * The socket connected to server.
     */
    private SSLSocket server;

    /**
     * The constructor is given the server's name. It opens a socket connection to the server and extracts it input and
     * out streams.
     *
     * @param serverName The server want to connect.
     * @param loginModel The controller of login page.
     * @param username   The user's username.
     * @param password   The user's password.
     */
    public ClientThread(String serverName, LoginModel loginModel, String username, String password) {
        this.serverName = serverName;
        this.loginModel = loginModel;
        this.username = username;
        this.password = password;

    }

    /**
     * Getter for username.
     *
     * @return The user's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for toServer.
     *
     * @return The output stream of connection.
     */
    public ObjectOutputStream getToServer() {
        return toServer;
    }

    /**
     * End connection when user log out.
     *
     * @throws IOException
     */
    public void endConnection() throws IOException {
        toServer.close();
        fromServer.close();
        server.close();
    }

    /**
     * Initialize TLS certification and created a SSL connection.
     *
     * @throws Exception Exception when initializing TLS.
     */
    private void initTLS() throws Exception {
        String CLIENT_KEY_STORE = "src/main/resources/socotra_client_ks";
        String CLIENT_KEY_STORE_PASSWORD = "socotra";
        System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
        // See handle shake process under debug.
//            System.setProperty("javax.net.debug", "ssl,handshake");

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(CLIENT_KEY_STORE), CLIENT_KEY_STORE_PASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        context.init(kmf.getKeyManagers(), trustManagers, null);
        SocketFactory factory = context.getSocketFactory();
        server = (SSLSocket) factory.createSocket(serverName, 50443);
        server.startHandshake();
    }

    /**
     * The thread's job.
     */
    public void run() {
        try {
            initTLS();
            toServer = new ObjectOutputStream(server.getOutputStream());
            fromServer = new ObjectInputStream(server.getInputStream());
            loginModel.setErrorType(0);
            toServer.writeObject(new ConnectionData(-1, username, password));
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromServer.readObject();
                System.out.println("Received connectionData.");
                switch (connectionData.getType()) {
                    // If connectionData is about the result of user's validation.
                    case -1:
                        if (!connectionData.getValidated()) {
                            loginModel.setErrorType(2);
                            endConnection();
                        }
                        synchronized (loginModel) {
                            loginModel.notify();
                        }
                        break;
                    // If connectionData is about users online information.
                    case -2:
                        System.out.println(connectionData.getUserSignature() + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                        if (connectionData.getIsOnline()) {
                            Client.getHomeModel().appendClientsList(connectionData.getUserSignature());
                        } else {
                            Client.getHomeModel().removeClientsList(connectionData.getUserSignature());
                        }
                        break;
                    // If connectionData is about set online users.
                    case -3:
                        System.out.println(connectionData.getOnlineUsers());
                        SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
                        Client.setSetOnlineUsers(setOnlineUsers);
                        setOnlineUsers.start();
                        break;
                    // If connectionData is about received hint.
                    case -4:
                        Client.getHomeModel().updateChatData(connectionData.getUuid(), connectionData.getChatSession());
                        break;
                    // If connectionData is about normal chat messages.
                    case 1:
                    case 2:
                        Client.getHomeModel().appendChatData(connectionData);
                        new SendThread(new ConnectionData(connectionData.getUuid(), this.username, connectionData.getChatSession())).start();
                        break;
                    // If connectionData is about chat history data.
                    case 3:
                        SetChatData setChatData = new SetChatData(connectionData.getChatData());
                        Client.setSetChatData(setChatData);
                        setChatData.start();
                        break;
                    default:
                        System.out.println("Unknown data.");
                }
            }
        } catch (IOException e) {
            loginModel.setErrorType(1);
            e.printStackTrace();
            System.out.println("Socket communication broke.");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected Error.");
        } finally {
            System.out.println("Finally handled.");
            // Notify the loginModel thread anyway.
            synchronized (loginModel) {
                loginModel.notify();
            }
            try {
                endConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
