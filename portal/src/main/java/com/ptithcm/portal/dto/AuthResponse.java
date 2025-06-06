package com.ptithcm.portal.dto;

public class AuthResponse {
    private String token;
    private String type;

    public AuthResponse(String token, String type) {
        this.token = token;
        this.type = type;
    }

    // Getter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
