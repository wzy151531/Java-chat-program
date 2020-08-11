package socotra.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import socotra.Client;
import socotra.util.Util;

public class ControllerUtil {

    /**
     * Load home page once log in.
     */
    public void loadHomePage() {
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

    public void loadBackUpPage() {
        // Load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/backup.fxml"));
        Pane tempPane = null;
        try {
            tempPane = loader.load();

            Client.setBackUpController(loader.getController());

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
