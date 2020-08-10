package socotra.model;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.whispersystems.libsignal.state.PreKeyRecord;
import socotra.Client;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.protocol.EncryptedClient;
import socotra.util.Util;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * This thread is used to communicate with server.
 */

public class ClientThread extends Thread {

    /**
     * Current connected server name.
     */
    private final String serverName;
    /**
     * Current user's username.
     */
    private final User user;
    /**
     * Current user's password.
     */
    private final String password;
    /**
     * The controller of login page.
     */
    private final LoginModel loginModel = Client.getLoginModel();
    private final SignUpModel signUpModel = Client.getSignUpModel();
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

    private int type;

    /**
     * The constructor is given the server's name. It opens a socket connection to the server and extracts it input and
     * out streams.
     *
     * @param serverName The server want to connect.
     * @param user       The user's information.
     * @param password   The user's password.
     */
    ClientThread(String serverName, User user, String password, int type) {
        this.serverName = serverName;
        this.user = user;
        this.password = password;
        this.type = type;
    }

    /**
     * Getter for username.
     *
     * @return The user's username.
     */
    public User getUser() {
        return user;
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
//        System.setProperty("javax.net.debug", "ssl,handshake");

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(CLIENT_KEY_STORE), CLIENT_KEY_STORE_PASSWORD.toCharArray());

//        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//        kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        // If server needn't verify client, kmf is not necessary.
//        context.init(kmf.getKeyManagers(), trustManagers, null);
        context.init(null, trustManagers, null);
        SocketFactory factory = context.getSocketFactory();
        server = (SSLSocket) factory.createSocket(serverName, 50443);
        server.startHandshake();
    }

    private void processLogin() throws IOException {
        toServer.writeObject(new ConnectionData(-1, user, password));
    }

    private KeyBundle generateKeyBundle(EncryptedClient encryptedClient) {
        int registrationId = encryptedClient.getRegistrationId();
        byte[] identityKey = encryptedClient.getIdentityKeyPair().getPublicKey().serialize();
        List<byte[]> preKeys = encryptedClient.getFlattenedPreKeys();
        int signedPreKeyId = encryptedClient.getSignedPreKey().getId();
        byte[] signedPreKey = encryptedClient.getSignedPreKey().getKeyPair().getPublicKey().serialize();
        byte[] signedPreKeySignature = encryptedClient.getSignedPreKey().getSignature();
        return new KeyBundle(registrationId, identityKey, preKeys, signedPreKeyId, signedPreKey, signedPreKeySignature);
    }

    private void processSignUp() throws IOException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        toServer.writeObject(new ConnectionData(user, password, generateKeyBundle(encryptedClient)));
    }

    private void handleIOException(IOException e) {
        Platform.runLater(() -> {
            Client.closeWaitingAlert();
            Alert errorAlert = Util.generateAlert(Alert.AlertType.ERROR, "Connection Error", "Server shuts down.", "Try later.");
            errorAlert.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    System.exit(0);
                }
                return null;
            });
            errorAlert.show();
        });
        System.out.println("Socket communication broke.");
    }

    private void handleNoHostException(IOException e) {
        Platform.runLater(() -> {
            Client.closeWaitingAlert();
            Util.generateAlert(Alert.AlertType.ERROR, "Connection Error", e instanceof ConnectException ? "Server closed" : "Invalidated Server", e instanceof ConnectException ? "Try later." : "Try again.").show();
        });
    }

    private void handleFinally() {
        System.out.println("Finally handled.");
        try {
            endConnection();
            System.out.println("Invalid User.");
        } catch (NullPointerException e) {
            System.out.println("Invalid Server.");
        } catch (IOException e) {
            System.out.println("IOException.");
        }
    }

    private void initAction() throws IOException {
        switch (type) {
            case 1:
                processLogin();
                break;
            case 2:
                processSignUp();
                break;
            default:
                System.out.println("Bad type.");
                break;
        }
    }

    public synchronized void sendData(ConnectionData connectionData) {
        try {
            toServer.writeObject(connectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The thread's job.
     */
    public void run() {
        try {
            initTLS();
            toServer = new ObjectOutputStream(server.getOutputStream());
            fromServer = new ObjectInputStream(server.getInputStream());
            initAction();
            DataHandler dataHandler = new DataHandler();
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromServer.readObject();
                if (!dataHandler.handle(connectionData)) {
                    return;
                }
            }
        } catch (NoRouteToHostException | UnknownHostException | ConnectException e) {
            handleNoHostException(e);
        } catch (IOException e) {
            e.printStackTrace();
            handleIOException(e);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected Error.");
        } finally {
            // TODO: invalidated server.
            handleFinally();
        }
    }

}
