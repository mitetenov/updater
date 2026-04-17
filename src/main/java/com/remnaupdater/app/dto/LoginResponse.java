package com.remnaupdater.app.dto;

public class LoginResponse {
    private TokenResponse response;

    public TokenResponse getResponse() {
        return response;
    }

    public void setResponse(TokenResponse response) {
        this.response = response;
    }

    public static class TokenResponse {
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}


