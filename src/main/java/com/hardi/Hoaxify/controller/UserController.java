package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.UserDTO;
import com.hardi.Hoaxify.domain.dto.UserUpdateDTO;
import com.hardi.Hoaxify.exceptions.ApiError;
import com.hardi.Hoaxify.service.UserService;
import com.hardi.Hoaxify.utils.CurrentUser;
import com.hardi.Hoaxify.utils.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public GenericResponse createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved successfully");
    }

    @GetMapping("/users")
    Page<UserDTO> getUsers(@CurrentUser User loggedInUser, @PageableDefault(size = 10) Pageable pageable) {
        return userService.getUsers(loggedInUser, pageable);
    }

    @GetMapping("/users/{username}")
    UserDTO getUserByName(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserDTO updateUser(@PathVariable Long id, @Valid @RequestBody(required = false) UserUpdateDTO userUpdate) throws IOException {
        return userService.update(id, userUpdate);
    }

}
