package socotra.util;

import socotra.Client;

import java.util.TreeSet;

public class SetOnlineUsers extends Thread {
    private TreeSet<String> onlineUsers;

    public SetOnlineUsers(TreeSet<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

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
