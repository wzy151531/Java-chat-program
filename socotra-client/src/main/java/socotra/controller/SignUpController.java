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
import socotra.common.User;
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
    private TextField deviceIdField;

    @FXML
    private Button submitButton;

    @FXML
    public void submit(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String repeat = repeatField.getText();
        String deviceId = deviceIdField.getText();
        if (Util.isEmpty(username) || Util.isEmpty(password)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Please input the info.").show();
            return;
        }
        if (!password.equals(repeat)) {
            Util.generateAlert(Alert.AlertType.WARNING, "Warning", "Validate Error.", "Repeat is not equal to password.").show();
            return;
        }
        User user = new User(username, Util.isEmpty(deviceId) ? 1 : Integer.parseInt(deviceId), true);
        Client.setSignUpModel(new SignUpModel());
        Client.getSignUpModel().handleSignUp(user, password);
        Client.showWaitingAlert();
    }

}
