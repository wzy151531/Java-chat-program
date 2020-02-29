package socotra.controller;

import com.vdurmont.emoji.EmojiParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import socotra.Client;
import socotra.service.ClientThread;

public class LoginController {

    @FXML
    private TextField serverField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    /**
     * Login event.
     *
     * @param event The login event.
     */
    @FXML
    public void login(ActionEvent event) {
        String serverStr = serverField.getText();
        String usernameStr = usernameField.getText();
        String passwordStr = passwordField.getText();
        if (isEmpty(usernameStr) || isEmpty(passwordStr)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Validate Error.");
            alert.setContentText("Please input the info.");
            alert.show();
        }
        loginButton.setText("login...");
        ClientThread clientThread = new ClientThread(isEmpty(serverStr) ? "localhost" : serverStr, this, usernameStr, passwordStr);
        clientThread.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Client.getErrorType() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Invalidated Server.");
            alert.setContentText("Try again.");
            alert.show();
            loginButton.setText("login");
        } else if (Client.getErrorType() == 2) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalidated user.");
            alert.setContentText("Try again.");
            alert.show();
            loginButton.setText("login");
        } else {
            // load .fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
            Pane tempPane = null;
            try {
                tempPane = loader.load();

                HomeController homeController = loader.getController();
                clientThread.setHomeController(homeController);
                homeController.setClientThread(clientThread);

                // construct scene
                Scene tempScene = new Scene(tempPane);
                // set scene
                Client.setScene(tempScene);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: hint if error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Unexpected Error.");
                alert.setContentText("Try again.");
                alert.show();
            }
        }

    }

    /**
     * Check if the String is empty.
     *
     * @param str The given String.
     * @return If the String is empty, return true; else return false.
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
