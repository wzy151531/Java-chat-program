package socotra.util;

import javafx.scene.control.Alert;

public abstract class Util {

    /**
     * Check if the String is empty.
     *
     * @param str The given String.
     * @return If the String is empty, return true; else return false.
     */
    public static boolean isEmpty(String str) {
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
    public static Alert generateAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

}
