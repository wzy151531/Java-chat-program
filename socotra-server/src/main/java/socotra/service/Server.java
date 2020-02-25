package socotra.service;

import socotra.jdbc.JdbcUtil;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/* Server - a simple example of a server, that accumulates a total
   Protocol: 
   - A client can send a non-zero integer, which gets added to the total.
   - The new total is sent back to the client.
   - Sending a zero terminates the connection.
   - Sending anything else is an error.
 */

public class Server {

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        ArrayList<ObjectOutputStream> clients = new ArrayList<>();
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
                ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());
                clients.add(toClient);
                // get stuck until somebody connects
                ServerThread s = new ServerThread(clients, clientSocket, toClient);
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

}
