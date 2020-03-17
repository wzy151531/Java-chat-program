package socotra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import socotra.controller.HomeController;
import socotra.controller.LoginController;
import socotra.model.ClientThread;
import socotra.model.HomeModel;
import socotra.model.LoginModel;
import socotra.util.SetChatData;
import socotra.util.SetOnlineUsers;

/**
 * This is the entry of the application.
 */

public class Client extends Application {

    /**
     * The stage to show in the screen.
     */
    private static Stage stage;
    /**
     * Controller of login page.
     */
    private static LoginController loginController;
    /**
     * Model of login page.
     */
    private static LoginModel loginModel;
    /**
     * Controller of home page.
     */
    private static HomeController homeController;
    /**
     * Model of home page.
     */
    private static HomeModel homeModel;
    /**
     * Communication thread with server.
     */
    private static ClientThread clientThread;
    /**
     * Set online users thread.
     */
    private static SetOnlineUsers setOnlineUsers;
    /**
     * Set chat data thread.
     */
    private static SetChatData setChatData;

    /**
     * Getter for loginController.
     *
     * @return Controller of login page.
     */
    public static LoginController getLoginController() {
        return loginController;
    }

    /**
     * Setter for loginController.
     *
     * @param loginController Controller of login page.
     */
    public static void setLoginController(LoginController loginController) {
        Client.loginController = loginController;
    }

    /**
     * Getter for loginModel.
     *
     * @return Model of login page.
     */
    public static LoginModel getLoginModel() {
        return loginModel;
    }

    /**
     * Setter for loginModel.
     *
     * @param loginModel Model of login page.
     */
    public static void setLoginModel(LoginModel loginModel) {
        Client.loginModel = loginModel;
    }

    /**
     * Getter for homeController.
     *
     * @return Controller of home page.
     */
    public static HomeController getHomeController() {
        return homeController;
    }

    /**
     * Setter for homeController.
     *
     * @param homeController Controller of home page.
     */
    public static void setHomeController(HomeController homeController) {
        Client.homeController = homeController;
    }

    /**
     * Getter for homeModel.
     *
     * @return Model of home page.
     */
    public static HomeModel getHomeModel() {
        return homeModel;
    }

    /**
     * Setter for homeModel.
     *
     * @param homeModel Model of home page.
     */
    public static void setHomeModel(HomeModel homeModel) {
        Client.homeModel = homeModel;
        if (setOnlineUsers != null) {
            synchronized (setOnlineUsers) {
                setOnlineUsers.notify();
            }
        }
        if (setChatData != null) {
            synchronized (setChatData) {
                setChatData.notify();
            }
        }
    }

    /**
     * Getter for clientThread.
     *
     * @return Communication thread with server.
     */
    public static ClientThread getClientThread() {
        return clientThread;
    }

    /**
     * Setter for clientThread.
     *
     * @param clientThread Communication thread with server.
     */
    public static void setClientThread(ClientThread clientThread) {
        Client.clientThread = clientThread;
    }

    /**
     * Setter for setOnlineUsers.
     *
     * @param setOnlineUsers Set online users thread.
     */
    public static void setSetOnlineUsers(SetOnlineUsers setOnlineUsers) {
        Client.setOnlineUsers = setOnlineUsers;
    }

    /**
     * Setter for setChatData.
     *
     * @param setChatData Set chat data thread.
     */
    public static void setSetChatData(SetChatData setChatData) {
        Client.setChatData = setChatData;
    }

    /**
     * The program entry.
     *
     * @param args The arguments of the entry.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Set the scene of the stage.
     *
     * @param scene The scene needs to be shown.
     */
    public static void setScene(Scene scene) {
        stage.setScene(scene);
    }

    /**
     * Start to show the GUI.
     *
     * @param primaryStage The stage to show in the screen.
     * @throws Exception The exception thrown by javafx program.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Pane tempPane = loader.load();
        setLoginController(loader.getController());
        // Create scene
        Scene tempScene = new Scene(tempPane);
        // Add scene to the stage
        primaryStage.setScene(tempScene);
        // Show the stage
        primaryStage.show();
        stage = primaryStage;
    }

}