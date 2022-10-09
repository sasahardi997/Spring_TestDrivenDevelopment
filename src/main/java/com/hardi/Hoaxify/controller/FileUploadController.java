package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {

    @Autowired
    FileService fileService;

    @PostMapping("/hoaxes/upload")
    FileAttachment uploadForHoax(MultipartFile file) {
        return fileService.saveAttachment(file);
    }
}
