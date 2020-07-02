package com.example.passwordmanager;

public class user_account {

    String account_name, username, password;

    public user_account(String account_name, String username, String password){
        this.account_name = account_name;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccount_name() {
        return account_name;
    }
}
