package socotra.service;

import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ServerThread extends Thread {
    // private Total total = null;
    private ArrayList<ObjectOutputStream> clients = null;
    private Socket client = null;
    private ObjectOutputStream toClient = null;

    public ServerThread(ArrayList<ObjectOutputStream> clients, Socket client, ObjectOutputStream toClient) {
        super("ServerThread");
        this.client = client;
        this.clients = clients;
        this.toClient = toClient;
    }

    public void run() {
        ObjectInputStream fromClient;

        try {
            fromClient = new ObjectInputStream(client.getInputStream());

            while (true) {
                ConnectionData connectionData = (ConnectionData) fromClient.readObject();
                System.out.println("Received data.");
                if (connectionData.getType() == 0) {
                    String username = connectionData.getUsername();
                    String password = connectionData.getPassword();
                    try {
                        ResultSet resultSet = JdbcUtil.inquire("select * from test_user where username='" + username + "' and password='" + password + "'");
                        if (!resultSet.next()) {
                            System.out.println("Invalidated user.");
                            toClient.writeObject(new ConnectionData(false));
                            clients.remove(toClient);
                            toClient.close();
                            fromClient.close();
                            client.close();
                            return;
                        } else {
                            System.out.println("Validated user.");
                            toClient.writeObject(new ConnectionData(true));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    for (ObjectOutputStream toClient : clients) {
                        toClient.writeObject(connectionData);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong. Ending service to client...");
            clients.remove(toClient);
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
