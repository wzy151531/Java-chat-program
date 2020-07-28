package socotra.common;

import java.io.Serializable;

public class User implements Serializable, Comparable<User> {

    private static final long serialVersionUID = 1L;
    private String username;
    private int deviceId;
    private boolean active;

    public User(String username, int deviceId, boolean active) {
        this.username = username;
        this.deviceId = deviceId;
        this.active = active;
    }

    public String getUsername() {
        return this.username;
    }

    public int getDeviceId() {
        return this.deviceId;
    }

    public boolean isActive() {
        return this.active;
    }

    @Override
    public String toString() {
        return this.username + ":" + this.deviceId;
    }

    @Override
    public int compareTo(User other) {
        int usernameResult = this.username.compareTo(other.getUsername());
        if (usernameResult == 0) {
            return this.deviceId - other.deviceId;
        }
        return usernameResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return this.username.equals(other.getUsername()) && this.deviceId == other.getDeviceId();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.username.hashCode();
        hash = hash * 31 + deviceId;
        return hash;
    }

}
