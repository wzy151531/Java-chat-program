package socotra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.controller.*;
import socotra.model.*;
import socotra.protocol.*;
import socotra.util.SetOnlineUsers;
import socotra.util.TestProtocol;
import socotra.util.Util;

import java.io.IOException;

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
    private static BackUpController backUpController;
    /**
     * Model of home page.
     */
    private static HomeModel homeModel;
    private static BackUpModel backUpModel;
    private static SignUpController signUpController;
    private static SignUpModel signUpModel;
    private static EncryptedClient encryptedClient;
    /**
     * Controller of SnakeGame page
     */
    private static socotra.controller.BoardController BoardController;
    /**
     * Model of Food, Sanke, SnakePart in SnakeGame.
     */
    private static socotra.model.Food Food;
    private static socotra.model.Snake Snake;
    private static SnakePart SnakePart;
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
    private static Alert waitingAlert = Util.generateAlert(Alert.AlertType.NONE, "Waiting", "Connecting To Server.", "Please Be Patient.");
    private static Alert initClientAlert = Util.generateAlert(Alert.AlertType.NONE, "Waiting", "Initializing Client.", "Please Be Patient.");
    private static Alert initGroupChatAlert = Util.generateAlert(Alert.AlertType.NONE, "Waiting", "Initializing Group Chat.", "Please Be Patient.");
    private static Alert initPairwiseChatAlert = Util.generateAlert(Alert.AlertType.NONE, "Waiting", "Initializing Pairwise Chat.", "Please Be Patient.");
    private static Alert reInitChatAlert = Util.generateAlert(Alert.AlertType.NONE, "Waiting", "ReInitializing Chat.", "Please Be Patient.");
    private static DataHandler dataHandler;

    public static User backUpReceiver;
    public static KeyBundle backUpKeyBundle;

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

    public static void setBackUpController(BackUpController backUpController) {
        Client.backUpController = backUpController;
    }

    public static SignUpController getSignUpController() {
        return signUpController;
    }

    public static void setSignUpController(SignUpController signUpController) {
        Client.signUpController = signUpController;
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
    }

    public static SignUpModel getSignUpModel() {
        return signUpModel;
    }

    public static void setSignUpModel(SignUpModel signUpModel) {
        Client.signUpModel = signUpModel;
    }

    public static EncryptedClient getEncryptedClient() {
        return encryptedClient;
    }

    public static void setEncryptedClient(EncryptedClient encryptedClient) {
        Client.encryptedClient = encryptedClient;
    }

    /**
     * Getter for BoardController.
     *
     * @return Controller of SnakeGame page.
     */
    public static BoardController getBoardController() {
        return BoardController;
    }

    /**
     * Setter for BoardController.
     *
     * @param boardController Controller of SnakeGame page.
     */
    public static void setBoardController(BoardController boardController) {
        Client.BoardController = BoardController;
    }

    /**
     * Getter for Food, Sanke, SnakePart in SnakeGame.
     *
     * @return
     */
    public static Food getFood() {
        return Food;
    }

    /**
     * Setter for Food, Snake, SnakePart in SnakeGame.
     *
     * @return
     */
    public static void setFood(Food food) {
        Client.Food = Food;
    }

    public static Snake getSnake() {
        return Snake;
    }

    public static void setSnake(Snake snake) {
        Client.Snake = Snake;
    }

    public static SnakePart getSnakePart() {
        return SnakePart;
    }

    public static void setSnakePart(SnakePart snakePart) {
        Client.SnakePart = SnakePart;
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

    public static void showWaitingAlert() {
        waitingAlert.setAlertType(Alert.AlertType.NONE);
        waitingAlert.show();
    }

    public static void closeWaitingAlert() {
        waitingAlert.setAlertType(Alert.AlertType.INFORMATION);
        waitingAlert.close();
    }

    public static void showInitGroupChatAlert() {
        initGroupChatAlert.setAlertType(Alert.AlertType.NONE);
        initGroupChatAlert.show();
    }

    public static void closeInitGroupChatAlert() {
        initGroupChatAlert.setAlertType(Alert.AlertType.INFORMATION);
        initGroupChatAlert.close();
    }

    public static void showInitPairwiseChatAlert() {
        initPairwiseChatAlert.setAlertType(Alert.AlertType.NONE);
        initPairwiseChatAlert.show();
    }

    public static void closeInitPairwiseChatAlert() {
        initPairwiseChatAlert.setAlertType(Alert.AlertType.INFORMATION);
        initPairwiseChatAlert.close();
    }

    public static void showReInitChatAlert() {
        reInitChatAlert.setAlertType(Alert.AlertType.NONE);
        reInitChatAlert.show();
    }

    public static void closeReInitChatAlert() {
        reInitChatAlert.setAlertType(Alert.AlertType.INFORMATION);
        reInitChatAlert.close();
    }

    public static void showInitClientAlert() {
        initClientAlert.setAlertType(Alert.AlertType.NONE);
        initClientAlert.show();
    }

    public static void closeInitClientAlert() {
        initClientAlert.setAlertType(Alert.AlertType.INFORMATION);
        initClientAlert.close();
    }

    public static DataHandler getDataHandler() {
        return Client.dataHandler;
    }

    public static void setDataHandler(DataHandler dataHandler) {
        Client.dataHandler = dataHandler;
    }

    public static void processLogout(ConnectionData connectionData) {
        try {
            Saver saver = new Saver(clientThread.getUser(), Client.getEncryptedClient());
            saver.saveStores();
            saver.saveChatData(Client.getHomeModel().getChatDataCopy());
            clientThread.sendData(connectionData);
            clientThread.endConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static BackUpModel getBackUpModel() {
        return Client.backUpModel;
    }

    public static void setBackUpModel(BackUpModel backUpModel) {
        Client.backUpModel = backUpModel;
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

//        TestProtocol.test();
    }

}
