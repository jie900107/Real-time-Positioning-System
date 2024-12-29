package com.example.real_timepositioningrescuesystem;

public class User {
    String avatar;
    String email;
    String username;

    public User(String avatar,String email,String username) {
        this.avatar = avatar;
        this.email = email;
        this.username = username;
    }
    public User(){

    }

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
