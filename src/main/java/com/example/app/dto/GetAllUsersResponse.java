package com.example.app.dto;

import java.util.List;

public class GetAllUsersResponse {
    private UsersWrapper response;

    public UsersWrapper getResponse() {
        return response;
    }

    public void setResponse(UsersWrapper response) {
        this.response = response;
    }

    public static class UsersWrapper {
        private List<UserDto> users;

        public List<UserDto> getUsers() {
            return users;
        }

        public void setUsers(List<UserDto> users) {
            this.users = users;
        }
    }
}


