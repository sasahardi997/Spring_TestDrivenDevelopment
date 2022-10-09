package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.UserDTO;
import com.hardi.Hoaxify.utils.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    UserDTO handleLogin(@CurrentUser User loggedInUser) {
        return new UserDTO(loggedInUser);
    }
}
