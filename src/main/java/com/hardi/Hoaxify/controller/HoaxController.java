package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.HoaxDTO;
import com.hardi.Hoaxify.domain.dto.UserDTO;
import com.hardi.Hoaxify.service.HoaxService;
import com.hardi.Hoaxify.utils.CurrentUser;
import com.hardi.Hoaxify.utils.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    HoaxDTO createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        return hoaxService.save(user, hoax);
    }

    @GetMapping("/hoaxes")
    Page<HoaxDTO> getHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable);
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<?> getHoaxesOfUser(@PathVariable String username,
                            Pageable pageable) {
        return hoaxService.getHoaxesOfUser(username, pageable);
    }

    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@PathVariable Long id,
                                        Pageable pageable,
                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction) {
        if(!direction.equalsIgnoreCase("after")){
            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, pageable));
        }
        if(count) {
            long newHoaxCount = hoaxService.getNewHoaxesCount(id);
            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
        }
        return ResponseEntity.ok(hoaxService.getNewHoaxes(id, pageable));
    }

    @GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getUserHoaxesRelative(@PathVariable Long id,
                               @PathVariable String username,
                               @RequestParam(name = "direction", defaultValue = "after") String direction,
                               @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
                               Pageable pageable) {
        if(!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(hoaxService.getOldUserHoaxes(id, username, pageable));
        }
        if(count) {
            long newHoaxCount = hoaxService.getNewUserHoaxesCount(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
        }
        return ResponseEntity.ok(hoaxService.getNewUserHoaxes(id, username, pageable));
    }

    @DeleteMapping("/hoaxes/{id:[0-9]+}")
    @PreAuthorize("@hoaxSecurityService.isAllowedToDelete(#id, principal)")
    GenericResponse deleteHoax(@PathVariable Long id) {
        hoaxService.deleteHoax(id);
        return new GenericResponse("Hoax is removed");
    }

}
