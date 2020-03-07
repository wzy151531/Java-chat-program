package socotra;

import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;
import socotra.service.ServerThread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    private static HashMap<String, ObjectOutputStream> clients = new HashMap<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

//        ArrayList<ObjectOutputStream> clients = new ArrayList<>();
        // Open a server socket:
        try {
            serverSocket = new ServerSocket(50000);
            System.out.println("Server bound.");
            JdbcUtil.init();
            JdbcUtil.connect();
        } catch (IOException e) {
            System.err.println("Couldn't listen on port: 50000.");
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listen to the socket, accepting connections from new clients,
        // and running a new thread to serve each new client:
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Got a connection.");
//                ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());
//                clients.add(toClient);
                // get stuck until somebody connects
//                ServerThread s = new ServerThread(clients, clientSocket, toClient);
                ServerThread s = new ServerThread(clientSocket);

                s.start();
            }
        } catch (Exception e) {
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

    public synchronized static void addClient(String username, ObjectOutputStream toClient) {
        Server.clients.put(username, toClient);
    }

    public synchronized static HashMap<String, ObjectOutputStream> getClients() {
        return Server.clients;
    }

    public synchronized static void removeClient(String username, ObjectOutputStream toClient) {
        Server.clients.remove(username, toClient);
    }

    public synchronized static void removeClient(String username) {
        Server.clients.remove(username);
    }

    public synchronized static void broadcast(ConnectionData connectionData) {
        Server.clients.forEach((k, v) -> {
            try {
                v.writeObject(connectionData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized static void broadcast(ConnectionData connectionData, String username, ConnectionData specialConnectionData) {
        Server.clients.forEach((k, v) -> {
            try {
                if (!k.equals(username)) {
                    v.writeObject(connectionData);
                } else {
                    v.writeObject(specialConnectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized static void broadcast(ConnectionData connectionData, String username) {
        Server.clients.forEach((k, v) -> {
            try {
                if (!k.equals(username)) {
                    v.writeObject(connectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void privateSend(ConnectionData connectionData, String username) {
        try {
            Server.clients.get(username).writeObject(connectionData);
            Server.clients.get(connectionData.getUserSignature()).writeObject(connectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
