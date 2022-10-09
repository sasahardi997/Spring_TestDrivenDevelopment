package com.hardi.Hoaxify.service;

import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.HoaxDTO;
import com.hardi.Hoaxify.exceptions.NotFoundException;
import com.hardi.Hoaxify.repository.FileAttachmentRepository;
import com.hardi.Hoaxify.repository.HoaxRepository;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.file.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    UserRepository userRepository;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    public HoaxService(HoaxRepository hoaxRepository, UserRepository userRepository,
                       FileAttachmentRepository fileAttachmentRepository, FileService fileService) {
        this.hoaxRepository = hoaxRepository;
        this.userRepository = userRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
    }

    public HoaxDTO save(User user, Hoax hoax) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        if(hoax.getAttachment() != null){
            FileAttachment inDB = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
            inDB.setHoax(hoax);
            hoax.setAttachment(inDB);
        }
        return new HoaxDTO(hoaxRepository.save(hoax));
    }

    public Page<HoaxDTO> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable).map(HoaxDTO::new);
    }

    public Page<HoaxDTO> getHoaxesOfUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username " + username + " doesn't exists"));

        return hoaxRepository.findByUser(user, pageable).map(HoaxDTO::new);
    }

    public Page<HoaxDTO> getOldHoaxes(Long id, Pageable pageable) {
        return hoaxRepository.findByIdLessThan(id, pageable).map(HoaxDTO::new);
    }

    public Page<?> getOldUserHoaxes(Long id, String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username " + username + " doesn't exists"));
        return hoaxRepository.findByIdLessThanAndUser(id, user, pageable).map(HoaxDTO::new);
    }

    public List<HoaxDTO> getNewHoaxes(Long id, Pageable pageable) {
        return hoaxRepository.findByIdGreaterThan(id, pageable.getSort())
                .stream().map(HoaxDTO::new).collect(Collectors.toList());
    }

    public List<HoaxDTO> getNewUserHoaxes(Long id, String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username " + username + " doesn't exists"));
        return hoaxRepository.findByIdGreaterThanAndUser(id, user, pageable.getSort())
                .stream().map(HoaxDTO::new).collect(Collectors.toList());
    }

    public long getNewHoaxesCount(Long id) {
        return hoaxRepository.countByIdGreaterThan(id);
    }

    public long getNewUserHoaxesCount(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with username " + username + " doesn't exists"));
        return hoaxRepository.countByIdGreaterThanAndUser(id, user);
    }

    public void deleteHoax(Long id) {
        Hoax hoax = hoaxRepository.getById(id);
        if(hoax.getAttachment() != null){
            fileService.deleteAttachmentImage(hoax.getAttachment().getName());
        }
        hoaxRepository.deleteById(id);
    }
}
