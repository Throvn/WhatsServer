package com.throvn;

public class User {
    private String ip;
    private int port;
    public int id;

    public User(String ip, int port, int id) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}