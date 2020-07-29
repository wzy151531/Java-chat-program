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
    private TreeSet<User> members;
    /**
     * The hint indicates whether the chat session receive new message.
     */
    private boolean hint;

    private boolean encrypted;

    private int sessionType;

    /**
     * Constructor for chatSession.
     *
     * @param members The users in chat session.
     * @param hint        The hint indicates whether the chat session receive new message.
     */

    /**
     * Constructor for chatSession.
     *
     * @param members     The users in chat session.
     * @param hint        The hint indicates whether the chat session receive new message.
     * @param encrypted   Whether chat session is encrypted.
     * @param sessionType The type of session.(group or pairwise)
     */
    public ChatSession(TreeSet<User> members, boolean hint, boolean encrypted, int sessionType) {
        this.members = members;
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
        this.members = new TreeSet<>();
        for (String user : users) {
            String[] userParts = user.split(":");
            User temp = new User(userParts[0], Integer.parseInt(userParts[1]), true);
            this.members.add(temp);
        }
        this.hint = false;
        this.encrypted = parts.length == 3;
    }

    public int getSessionType() {
        return this.sessionType;
    }

    /**
     * Getter for members.
     *
     * @return The users in chat session.
     */
    public TreeSet<User> getMembers() {
        return members;
    }

    /**
     * Setter for members.
     *
     * @param members The users in chat session.
     */
    public void setMembers(TreeSet<User> members) {
        this.members = members;
    }

    public User relatedUser(User user) {
        for (User other : members) {
            if (!other.equals(user) && other.getUsername().equals(user.getUsername())) {
                return other;
            }
        }
        return null;
    }

    public String generateChatName(User caller) {
        TreeSet<User> copy = new TreeSet<>(this.members);
        copy.remove(caller);
        TreeSet<String> membersName = new TreeSet<>();
        copy.forEach(n -> {
            membersName.add(n.getUsername());
        });
        String temp = membersName.toString();
        String chatName = temp.substring(1, temp.length() - 1);
        return this.isEncrypted() ? "🔒" + chatName : chatName;
    }

    public String generateChatIdCSV() {
        String temp = "";
        for (User user : members) {
            temp = temp + user + "|";
        }
        String result = temp.substring(0, temp.length() - 1);
        result = result + "/" + sessionType;
        return this.isEncrypted() ? "🔒/" + result : result;
    }

    public TreeSet<User> getOthers(User caller) {
        TreeSet<User> result = new TreeSet<>(this.members);
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
            return this.members.equals(chatSession.members) &&
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
        int hash = 17;
        hash = hash * 31 + this.members.hashCode();
        hash = hash * 31 + (this.encrypted ? 0 : 1);
        hash = hash * 31 + this.sessionType;
        return hash;
    }

}
