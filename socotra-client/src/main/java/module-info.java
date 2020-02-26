module socotra.client.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.desktop;

    opens socotra.controller to javafx.fxml;
    exports socotra;
}