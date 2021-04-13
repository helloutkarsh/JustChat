package com.santara.justchat.Models;

public class User {

    private String userId, userName, userPhone, userDP;

    public User(){      //Empty constructor is needed when we are dealing with firebase apps
        
    }

    public User(String userId, String userName, String userPhone, String userDP) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userDP = userDP;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserDP() {
        return userDP;
    }

    public void setUserDP(String userDP) {
        this.userDP = userDP;
    }
}
