package socotra.util;

import socotra.Client;

import java.util.TreeSet;

/**
 * This thread is used to set online users when user log in.
 */

public class SetOnlineUsers extends Thread {

    /**
     * Current online users.
     */
    private TreeSet<String> onlineUsers;

    /**
     * Constructor for setOnlineUsers.
     *
     * @param onlineUsers Current online users.
     */
    public SetOnlineUsers(TreeSet<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    /**
     * Start setting the online users. If homeModel is not created, then this thread wait.
     */
    public void run() {
        if (Client.getHomeModel() == null) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Client.getHomeModel().appendClientsList("all");
        onlineUsers.forEach(n -> {
            Client.getHomeModel().appendClientsList(n);
        });
    }
}
