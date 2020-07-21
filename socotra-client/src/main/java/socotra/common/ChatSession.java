package socotra.common;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * This file defines the format of chat session.
 */

public class ChatSession implements Serializable {

    public static final int PAIRWISE = 1;
    public static final int GROUP = 2;

    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The users in chat session.
     */
    private TreeSet<String> toUsernames;
    /**
     * The hint indicates whether the chat session receive new message.
     */
    private boolean hint;

    private boolean encrypted;

    private int sessionType;

    /**
     * Constructor for chatSession.
     *
     * @param toUsernames The users in chat session.
     * @param hint        The hint indicates whether the chat session receive new message.
     */

    /**
     * Constructor for chatSession.
     *
     * @param toUsernames The users in chat session.
     * @param hint        The hint indicates whether the chat session receive new message.
     * @param encrypted   Whether chat session is encrypted.
     * @param sessionType The type of session.(group or pairwise)
     */
    public ChatSession(TreeSet<String> toUsernames, boolean hint, boolean encrypted, int sessionType) {
        this.toUsernames = toUsernames;
        this.hint = hint;
        this.encrypted = encrypted;
        this.sessionType = sessionType;
    }

    public ChatSession(String chatIdCSV) {
        String[] parts = chatIdCSV.split("/");
        if (parts.length != 3 && parts.length != 2) {
            throw new IllegalArgumentException("Bad chatIdCSV.");
        }
        this.sessionType = Integer.parseInt(parts[parts.length - 1]);
        String[] users = parts[parts.length - 2].split("\\|");
        this.toUsernames = new TreeSet<>();
        for (String username : users) {
            this.toUsernames.add(username);
        }
        this.hint = false;
        this.encrypted = parts.length == 3;
    }

    public int getSessionType() {
        return this.sessionType;
    }

    /**
     * Getter for toUsernames.
     *
     * @return The users in chat session.
     */
    public TreeSet<String> getToUsernames() {
        return toUsernames;
    }

    /**
     * Setter for toUsernames.
     *
     * @param toUsernames The users in chat session.
     */
    public void setToUsernames(TreeSet<String> toUsernames) {
        this.toUsernames = toUsernames;
    }

    public String generateChatName(String caller) {
        TreeSet<String> copy = new TreeSet<>(this.toUsernames);
        copy.remove(caller);
        String temp = copy.toString();
        String chatName = temp.substring(1, temp.length() - 1);
        return this.isEncrypted() ? "🔒" + chatName : chatName;
    }

    public String generateChatId() {
        TreeSet<String> copy = new TreeSet<>(toUsernames);
        String temp = copy.toString();
        String result = temp.substring(1, temp.length() - 1);
        return this.isEncrypted() ? "🔒" + result : result;
    }

    public String generateChatIdCSV() {
        String temp = "";
        for (String username : toUsernames) {
            temp = temp + username + "|";
        }
        String result = temp.substring(0, temp.length() - 1);
        result = result + "/" + sessionType;
        return this.isEncrypted() ? "🔒/" + result : result;
    }

    public TreeSet<String> getOthers(String caller) {
        TreeSet<String> result = new TreeSet<>(this.toUsernames);
        result.remove(caller);
        return result;
    }

    /**
     * Getter for hint.
     *
     * @return The hint indicates whether the chat session receive new message.
     */
    public boolean isHint() {
        return hint;
    }

    /**
     * Setter for hint.
     *
     * @param hint The hint indicates whether the chat session receive new message.
     */
    public void setHint(boolean hint) {
        this.hint = hint;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Define own equal logic.
     *
     * @param o The object compares to.
     * @return The boolean indicates whether two chatSession equal.
     */
    @Override
    public boolean equals(Object o) {
        ChatSession chatSession = (ChatSession) o;
        try {
            return this.toUsernames.equals(chatSession.toUsernames) &&
                    this.encrypted == chatSession.isEncrypted() &&
                    this.sessionType == chatSession.getSessionType();
        } catch (NullPointerException e) {
//            e.printStackTrace();
            return false;
        }
    }

    /**
     * Define own hashCode logic.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return this.toUsernames.hashCode();
    }

}
