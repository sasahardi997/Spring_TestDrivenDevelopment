package com.hardi.Hoaxify.service;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.UserDTO;
import com.hardi.Hoaxify.domain.dto.UserUpdateDTO;
import com.hardi.Hoaxify.exceptions.NotFoundException;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileService fileService;

    public User save(User user) {
//        User inDB = userRepository.findByUsername(user.getUsername())
//                .orElseThrow(() -> new DuplicateUsernameException("Username already exists"));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<UserDTO> getUsers(User loggedInUser, Pageable pageable) {
        if(loggedInUser != null){
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable).map(UserDTO::new);
        }
        return userRepository.findAll(pageable).map(UserDTO::new);
    }

    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username " + username + " doesn't exists"));
        return new UserDTO(user);
    }

    public UserDTO update(Long id, UserUpdateDTO userUpdate) {
        User inDB = userRepository.getById(id);
        inDB.setDisplayName(userUpdate.getDisplayName());
        if(userUpdate.getImage() != null){
            String savedImageName;
            try {
                fileService.deleteProfileImage(inDB.getImage());
                savedImageName = fileService.saveProfileImage(userUpdate.getImage());
                inDB.setImage(savedImageName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        User updated = userRepository.save(inDB);
        return new UserDTO(updated);
    }
}
