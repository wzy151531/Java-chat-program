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
import socotra.model.LoginModel;
import socotra.util.Util;

/**
 * This file is about controller of login page.
 */

public class LoginController {

    /**
     * The server address input.
     */
    @FXML
    private TextField serverField;
    /**
     * The username input.
     */
    @FXML
    private TextField usernameField;
    /**
     * The password input.
     */
    @FXML
    private PasswordField passwordField;
    /**
     * The login button.
     */
    @FXML
    private Button loginButton;

    /**
     * Login event of loginButton.
     *
     * @param event The login event.
     */
    @FXML
    public void login(ActionEvent event) {
        String serverName = serverField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (Util.isEmpty(username) || Util.isEmpty(password)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.").show();
            return;
        }
        loginButton.setText("login...");
        Client.setLoginModel(new LoginModel());
        int errorType = Client.getLoginModel().handleLogin(serverName, username, password);
        loginButton.setText("login");
        switch (errorType) {
            // If the server name is not correct.
            case 1:
                Util.generateAlert(Alert.AlertType.ERROR, "Connection Error", "Invalidated Server.", "Try again.").show();
                break;
            // If the user is invalidated.
            case 2:
                Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "Invalidated user.", "Try again.").show();
                break;
            default:
                loadHomePage();
                break;
        }
    }

    /**
     * Load home page once log in.
     */
    private void loadHomePage() {
        // Load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
        Pane tempPane = null;
        try {
            tempPane = loader.load();

            Client.setHomeController(loader.getController());

            // Construct scene
            Scene tempScene = new Scene(tempPane);
            // Set scene
            Client.setScene(tempScene);
        } catch (Exception e) {
            e.printStackTrace();
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error.", "Try again.").show();
        }
    }

}
