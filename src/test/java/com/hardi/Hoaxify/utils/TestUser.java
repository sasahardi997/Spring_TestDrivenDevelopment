package com.hardi.Hoaxify.utils;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.UserUpdateDTO;

public class TestUser {

    public static User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");
        user.setImage("profile-image.png");
        return user;
    }

    public static User createCustomUser(String username) {
        User user = createValidUser();
        user.setUsername(username);
        return user;
    }

    public static UserUpdateDTO createUserUpdateDTO() {
        UserUpdateDTO updatedUser = new UserUpdateDTO();
        updatedUser.setDisplayName("test-display-name");
        return updatedUser;
    }

    public static UserUpdateDTO createUserUpdateDTO(String username) {
        UserUpdateDTO updatedUser = new UserUpdateDTO();
        updatedUser.setDisplayName(username);
        return updatedUser;
    }

}
