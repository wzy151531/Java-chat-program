package socotra.common;

import java.io.Serializable;
import java.util.TreeSet;

public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private TreeSet<String> toUsernames;
    private boolean hint;

    public ChatSession(TreeSet<String> toUsernames, boolean hint) {
        this.toUsernames = toUsernames;
        this.hint = hint;
    }

    public TreeSet<String> getToUsernames() {
        return toUsernames;
    }

    public void setToUsernames(TreeSet<String> toUsernames) {
        this.toUsernames = toUsernames;
    }

    public boolean isHint() {
        return hint;
    }

    public void setHint(boolean hint) {
        this.hint = hint;
    }

    @Override
    public boolean equals(Object o) {
        ChatSession chatSession = (ChatSession) o;
        return this.toUsernames.equals(chatSession.toUsernames);
    }

    @Override
    public int hashCode() {
        return this.toUsernames.hashCode();
    }

}
