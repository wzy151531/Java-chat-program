package socotra.common;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * This file defines the format of chat session.
 */

public class ChatSession implements Serializable {

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

    /**
     * Constructor for chatSession.
     *
     * @param toUsernames The users in chat session.
     * @param hint        The hint indicates whether the chat session receive new message.
     */
    public ChatSession(TreeSet<String> toUsernames, boolean hint) {
        this.toUsernames = toUsernames;
        this.hint = hint;
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
            return this.toUsernames.equals(chatSession.toUsernames);
        } catch (NullPointerException e) {
            e.printStackTrace();
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
