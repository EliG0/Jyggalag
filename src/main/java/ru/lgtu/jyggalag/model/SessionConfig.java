package ru.lgtu.jyggalag.model;

public class SessionConfig {
    private boolean rememberMe = false;
    private int savedUserId = -1;

    public SessionConfig() {}

    public SessionConfig(boolean rememberMe, int savedUserId) {
        this.rememberMe = rememberMe;
        this.savedUserId = savedUserId;
    }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }

    public int getSavedUserId() { return savedUserId; }
    public void setSavedUserId(int savedUserId) { this.savedUserId = savedUserId; }
}