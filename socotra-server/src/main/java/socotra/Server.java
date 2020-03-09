package socotra;

import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;
import socotra.service.ServerThread;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.HashMap;

public class Server {

    private static HashMap<String, ObjectOutputStream> clients = new HashMap<>();
    private static SSLServerSocket serverSocket;

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

    public static void main(String[] args) {
        // Open a server socket:
        try {
            initTLS();
            System.out.println("Server bound.");
            JdbcUtil.init();
            JdbcUtil.connect();
        } catch (IOException e) {
            System.err.println("Couldn't listen on port: 50443.");
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listen to the socket, accepting connections from new clients,
        // and running a new thread to serve each new client:
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

    public static void privateSend(ConnectionData connectionData, String toUsername) {
        try {
            Server.clients.get(toUsername).writeObject(connectionData);
//            Server.clients.get(connectionData.getUserSignature()).writeObject(connectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
