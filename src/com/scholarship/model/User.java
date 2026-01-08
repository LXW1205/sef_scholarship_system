package com.scholarship.model;

public abstract class User {
    protected int id;
    protected String username;
    protected String email;
    protected String password;
    protected String role;
    protected boolean isActive;

    public User() {}

    public User(int id, String username, String email, String role, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public abstract boolean login();
    public abstract void logout();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
