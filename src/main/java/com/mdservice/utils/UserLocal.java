package com.mdservice.utils;

import com.mdservice.entity.User;

public class UserLocal {
    private static ThreadLocal<String> userLocal = new ThreadLocal<>();
    public static String getUser() {
        return userLocal.get();
    }
    public static void setUser(String id) {
        userLocal.set(id);
    }
    public static void removeUser() {
        userLocal.remove();
    }
}
