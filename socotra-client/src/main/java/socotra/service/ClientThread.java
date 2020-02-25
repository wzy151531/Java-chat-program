package socotra.service;// Usage:
//        java Client hostname
// type a non-zero integer on a line to send it to server
// type a blank line to end client

import socotra.Client;
import socotra.common.ConnectionData;
import socotra.controller.HomeController;
import socotra.controller.LoginController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread {

    private final LoginController loginController;
    private Socket server;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    private String serverName;
    private String username;
    private String password;
    private HomeController homeController;

    // The constructor is given the server's name It opens a socket
    // connection to the server and extracts it input and out streams.

    public ClientThread(String serverName, LoginController loginController, String username, String password) {
        this.serverName = serverName;
        this.loginController = loginController;
        this.username = username;
        this.password = password;

    }

    public void setHomeController(HomeController homeController) {
        System.out.println("setHomeController");
        this.homeController = homeController;
    }

    public String getUsername() {
        return username;
    }

    // finalize method (also called the "destructor")
    // closes all the resources used by the Client before the Client
    // gets destroyed (reclaimed by the garbage collector)
    public void finalize() {
        try {
            // Let server know we are done.
            // Our convention is to send "0" to indicate this.

            // toServer.writeInt(0);

            // Close the streams:

            toServer.close();
            fromServer.close();

            // Close the connection:

            server.close();
        } catch (IOException e) {
            error("Something went wrong ending the client");
        }
    }

    public ObjectOutputStream getToServer() {
        return toServer;
    }

    // This is what this class does:
    public void run() {
        try {
            server = new Socket(serverName, 50000);
            toServer = new ObjectOutputStream(server.getOutputStream());
            fromServer = new ObjectInputStream(server.getInputStream());
            Client.setErrorType(0);
            toServer.writeObject(new ConnectionData(-1, username, password));
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromServer.readObject();
                System.out.println("Received connectionData.");
                if (connectionData.getType() == -1) {
                    if (!connectionData.getValidated()) {
                        Client.setErrorType(2);
                        toServer.close();
                        fromServer.close();
                        server.close();
                    }
                    synchronized (loginController) {
                        loginController.notify();
                    }
                } else {
                    homeController.setConnectionData(connectionData);
                }
            }
        } catch (UnknownHostException e) {
            error("Unknown host: " + serverName);
        } catch (IOException e) {
            Client.setErrorType(1);
            e.printStackTrace();
            tell("Socket commmunication broke");
            // finalize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            synchronized (loginController) {
                loginController.notify();
            }
        }
    }

    // helper method to print error messages
    private void error(String message) {
        System.err.println(message);
        System.exit(1); // Don't do this in practice! (Why?)
    }

    // helper method to talk to the user
    private void tell(String message) {
        System.out.println(message);
    }

}
