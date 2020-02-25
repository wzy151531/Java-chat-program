package socotra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Client extends Application {

    private static Stage stage;
    private static int errorType = 1;

    public static void main(String[] args) {
        launch(args);
    }

    public static void setScene(Scene scene) {
        stage.setScene(scene);
    }

    public static int getErrorType() {
        return errorType;
    }

    public static void setErrorType(int errorType) {
        Client.errorType = errorType;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Pane tempPane = loader.load();

        // create scene
        Scene tempScene = new Scene(tempPane);

        // add scene to the stage
        primaryStage.setScene(tempScene);

        // show the stage
        primaryStage.show();

        stage = primaryStage;
    }

}