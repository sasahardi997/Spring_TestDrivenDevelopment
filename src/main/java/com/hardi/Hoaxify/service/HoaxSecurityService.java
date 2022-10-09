package com.hardi.Hoaxify.service;

import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.repository.HoaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class HoaxSecurityService {

    @Autowired
    HoaxRepository hoaxRepository;

    public boolean isAllowedToDelete(Long hoaxId, User loggedInUser) {
        Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
        if(optionalHoax.isPresent()) {
            Hoax inDb = optionalHoax.get();
            return Objects.equals(inDb.getUser().getId(), loggedInUser.getId());
        }
        return false;
    }
}
