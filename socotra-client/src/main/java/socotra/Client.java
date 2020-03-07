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
import socotra.util.SetOnlineUsers;

public class Client extends Application {

    /**
     * The stage to show in the screen.
     */
    private static Stage stage;

    // Put all the controllers and models in Client to manage.

    private static LoginController loginController;
    private static LoginModel loginModel;
    private static HomeController homeController;
    private static HomeModel homeModel;
    private static ClientThread clientThread;
    private static SetOnlineUsers setOnlineUsers;

    public static LoginController getLoginController() {
        return loginController;
    }

    public static void setLoginController(LoginController loginController) {
        Client.loginController = loginController;
    }

    public static LoginModel getLoginModel() {
        return loginModel;
    }

    public static void setLoginModel(LoginModel loginModel) {
        Client.loginModel = loginModel;
    }

    public static HomeController getHomeController() {
        return homeController;
    }

    public static void setHomeController(HomeController homeController) {
        Client.homeController = homeController;
    }

    public static HomeModel getHomeModel() {
        return homeModel;
    }

    public static void setHomeModel(HomeModel homeModel) {
        Client.homeModel = homeModel;
        synchronized (setOnlineUsers) {
            setOnlineUsers.notify();
        }
    }

    public static ClientThread getClientThread() {
        return clientThread;
    }

    public static void setClientThread(ClientThread clientThread) {
        Client.clientThread = clientThread;
    }

    public static void setSetOnlineUsers(SetOnlineUsers setOnlineUsers) {
        Client.setOnlineUsers = setOnlineUsers;
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