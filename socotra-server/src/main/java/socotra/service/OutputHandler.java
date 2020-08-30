package socotra.service;

import socotra.common.ConnectionData;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class OutputHandler {

    private final ObjectOutputStream oos;

    public OutputHandler(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public synchronized void sendMsg(ConnectionData connectionData) {
        try {
            this.oos.writeObject(connectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
