package socotra.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.Pane;
import socotra.Client;
import socotra.model.LoginModel;
import socotra.model.SignUpModel;
import socotra.util.Util;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatField;

    @FXML
    private Button submitButton;

    @FXML
    public void submit(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String repeat = repeatField.getText();
        if (!password.equals(repeat)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Repeat is not equal to password.").show();
            return;
        }
        Client.setSignUpModel(new SignUpModel());
        int errorType = Client.getSignUpModel().handleSignUp(username, password);
        switch (errorType) {
            // If the server name is not correct.
            case 1:
                Util.generateAlert(Alert.AlertType.ERROR, "Connection Error", "Invalidated Server.", "Try again.").show();
                break;
            // If the user is invalidated.
            case 2:
                Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "User Already Exists.", "Try another username.").show();
                break;
            default:
                ControllerUtil controllerUtil = new ControllerUtil();
                controllerUtil.loadHomePage();
                break;
        }
    }

}
