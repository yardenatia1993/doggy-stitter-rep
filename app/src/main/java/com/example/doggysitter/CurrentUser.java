package com.example.doggysitter;

import android.app.Application;

public class CurrentUser extends Application {
    private static CurrentUser instance;
    private User user;

    private CurrentUser() {}

    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public boolean isOwner() {
        return user instanceof UserOwner;
    }

    public boolean isWalker() {
        return user instanceof UserWalker;
    }
}
