package socotra.model;

import socotra.Client;
import socotra.common.ConnectionData;
import socotra.util.SendThread;
import socotra.util.SetChatData;
import socotra.util.SetOnlineUsers;

public class DataHandler {

    private final LoginModel loginModel = Client.getLoginModel();
    private final SignUpModel signUpModel = Client.getSignUpModel();

//    DataHandler(LoginModel loginModel, SignUpModel signUpModel) {
//        this.loginModel = loginModel;
//        this.signUpModel = signUpModel;
//    }

    boolean handle(ConnectionData connectionData) {
        switch (connectionData.getType()) {
            // If connectionData is about the result of user's validation.
            case -1:
                if (!connectionData.getValidated()) {
                    loginModel.setErrorType(2);
                    return false;
                }
                synchronized (loginModel) {
                    loginModel.notify();
                }
//                        TestProtocol.init();
                break;
            // If connectionData is about users online information.
            case -2:
                System.out.println(connectionData.getUserSignature() + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                if (connectionData.getIsOnline()) {
                    Client.getHomeModel().appendClientsList(connectionData.getUserSignature());
                } else {
                    Client.getHomeModel().removeClientsList(connectionData.getUserSignature());
                }
                break;
            // If connectionData is about set online users.
            case -3:
                System.out.println(connectionData.getOnlineUsers());
                SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
                Client.setSetOnlineUsers(setOnlineUsers);
                setOnlineUsers.start();
//                        TestProtocol.query();
                break;
            // If connectionData is about received hint.
            case -4:
                Client.getHomeModel().updateChatData(connectionData.getUuid(), connectionData.getChatSession());
                break;
            // If connectionData is about sign up result.
            case -5:
                if (!connectionData.getSignUpSuccess()) {
                    signUpModel.setErrorType(2);
                    return false;
                }
                synchronized (signUpModel) {
                    signUpModel.notify();
                }
                break;
            // If connectionData is about normal chat messages.
            case 1:
            case 2:
                Client.getHomeModel().appendChatData(connectionData);
                new SendThread(new ConnectionData(connectionData.getUuid(), Client.getClientThread().getUsername(), connectionData.getChatSession())).start();
                break;
            // If connectionData is about chat history data.
            case 3:
                SetChatData setChatData = new SetChatData(connectionData.getChatData());
                Client.setSetChatData(setChatData);
                setChatData.start();
                break;
            case 6:
                System.out.println("remote: " + new String(connectionData.getKeyBundle().getIdentityKey()));
//                        TestProtocol.identityKey = connectionData.getKeyBundle().getIdentityKey();
//                        TestProtocol.preKey = connectionData.getKeyBundle().getPreKey();
//                        TestProtocol.test();
                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

}
