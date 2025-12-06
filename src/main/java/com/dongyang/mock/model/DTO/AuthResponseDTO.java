package com.dongyang.mock.model.DTO;

public class AuthResponseDTO {
    private boolean success;
    private String accessToken;
    private String message;

    public AuthResponseDTO(boolean success, String accessToken, String message) {
        this.success = success;
        this.accessToken = accessToken;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getMessage() {
        return message;
    }
}