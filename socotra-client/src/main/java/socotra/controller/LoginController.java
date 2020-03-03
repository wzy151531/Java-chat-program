package socotra.controller;

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
import socotra.model.ClientThread;

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
     * Login event of loginButton.
     *
     * @param event The login event.
     */
    @FXML
    public void login(ActionEvent event) {
        String serverStr = serverField.getText();
        String usernameStr = usernameField.getText();
        String passwordStr = passwordField.getText();
        if (isEmpty(usernameStr) || isEmpty(passwordStr)) {
            Alert alert = generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.");
            alert.show();
            return;
        }
        loginButton.setText("login...");
        ClientThread clientThread = new ClientThread(isEmpty(serverStr) ? "localhost" : serverStr, this, usernameStr, passwordStr);
        clientThread.start();
        // Wait until the ClientThread notify it.
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // If the server name is not correct.
        if (Client.getErrorType() == 1) {
            Alert alert = generateAlert(Alert.AlertType.ERROR, "Connection Error", "Invalidated Server.", "Try again.");
            alert.show();
            loginButton.setText("login");
        } else if (Client.getErrorType() == 2) {
            // If the user is invalidated.
            Alert alert = generateAlert(Alert.AlertType.ERROR, "Validation Error", "Invalidated user.", "Try again.");
            alert.show();
            loginButton.setText("login");
        } else {
            // Load .fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
            Pane tempPane = null;
            try {
                tempPane = loader.load();

                HomeController homeController = loader.getController();
                clientThread.setHomeController(homeController);
                homeController.setClientThread(clientThread);

                // Construct scene
                Scene tempScene = new Scene(tempPane);
                // Set scene
                Client.setScene(tempScene);
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = generateAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error.", "Try again.");
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

    /**
     * Generate Alert panel.
     *
     * @param type    The alert type.
     * @param title   The title text of alert panel.
     * @param header  The header text of alert panel.
     * @param content The content text of alert panel.
     * @return The alert panel.
     */
    private Alert generateAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

}
