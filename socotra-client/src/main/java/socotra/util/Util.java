package socotra.util;

import javafx.scene.control.Alert;

import java.util.TreeSet;

/**
 * This is a tool class including some tool functions.
 */

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

    /**
     * Generate chat session name according to the toUsernames in chatSession.
     *
     * @param toUsernames toUsernames in chatSession.
     * @return The String represent the chatSession.
     */
    public static String generateChatName(TreeSet<String> toUsernames) {
        String result = toUsernames.toString();
        return result.substring(1, result.length() - 1);
    }

}
