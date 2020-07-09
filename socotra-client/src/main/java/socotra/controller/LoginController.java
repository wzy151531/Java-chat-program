package socotra.controller;

import javafx.animation.RotateTransition;
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

import java.io.IOException;

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
     * The signUp button.
     */
    @FXML
    private Button signUpButton;

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
        Client.setLoginModel(new LoginModel());
        Client.getLoginModel().handleLogin(serverName, username, password);
        Client.showWaitingAlert();
    }

    /**
     * Load sign up page.
     *
     * @param event The sign up button click event.
     */
    public void signUp(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signUp.fxml"));
        Pane tempPane = null;
        try {
            tempPane = loader.load();

            Client.setSignUpController(loader.getController());

            // Construct scene
            Scene tempScene = new Scene(tempPane);
            // Set scene
            Client.setScene(tempScene);
        } catch (IOException e) {
            e.printStackTrace();
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error.", "Try again.").show();
        }
    }

}
