package com.dongyang.mock.util.api;

import java.util.Date;

public class ApiToken {
    public final String token;
    public final long expiryTime;

    public ApiToken(String token, long expiresInSeconds) {
        this.token = token;
        this.expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000) - 60000;
        System.out.println("Token saved. Expiry Time: " + new Date(this.expiryTime));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryTime;
    }
}