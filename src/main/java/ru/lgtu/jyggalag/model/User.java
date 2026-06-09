package ru.lgtu.jyggalag.model;

import java.time.LocalDateTime;


public class User {
    private int id;
    private String username;
    private String passwordHash;
    private LocalDateTime registrationDate;
    private Theme currentTheme = Theme.CLASSIC;

    public User() {
    }

    public User(int id, String username, String passwordHash) {
        this(id, username, passwordHash, null, null);
    }

    public User(int id, String username, String passwordHash, LocalDateTime registrationDate, Theme currentTheme) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.registrationDate = registrationDate == null ? LocalDateTime.now() : registrationDate;
        this.currentTheme = currentTheme == null ? Theme.CLASSIC : currentTheme;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }

    @Override
    public String toString() {
        return username;
    }
}
