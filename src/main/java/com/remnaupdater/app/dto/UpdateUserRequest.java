package com.remnaupdater.app.dto;

public class UpdateUserRequest {
    private String uuid;
    private String description; // nullable in API

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


