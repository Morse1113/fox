package com.github.wenbo2018.fox.config.api.dto;

import java.io.Serializable;

/**
 * Created by shenwenbo on 2016/9/17.
 */
public class User implements Serializable{

    private static final long serialVersionUID = -2005236441469993193L;

    private  String username;
    private  String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
