package socotra.model;

import socotra.Client;
import socotra.common.ConnectionData;
import socotra.controller.HomeController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
     * The thread's job.
     */
    public void run() {
        try {
            Socket server = new Socket(serverName, 50000);
            toServer = new ObjectOutputStream(server.getOutputStream());
            fromServer = new ObjectInputStream(server.getInputStream());
            loginModel.setErrorType(0);
            toServer.writeObject(new ConnectionData(-1, username, password));
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromServer.readObject();
                System.out.println("Received connectionData.");
                // If receive the result about user's validation.
                if (connectionData.getType() == -1) {
                    if (!connectionData.getValidated()) {
                        loginModel.setErrorType(2);
                        toServer.close();
                        fromServer.close();
                        server.close();
                    }
                    // Notify the loginModel thread to redirect to the home page.
                    synchronized (loginModel) {
                        loginModel.notify();
                    }
                } else {
                    // Receive the normal messages from server.
                    Client.getHomeModel().appendHistoryData(connectionData);
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
        }
    }

}
