package com.hardi.Hoaxify.utils;

import com.hardi.Hoaxify.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class ProfileImageValidator implements ConstraintValidator<ProfileImage, String> {

    @Autowired
    FileService fileService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }

        byte[] decodedBytes = Base64.getDecoder().decode(value);
        String fileType = fileService.detectType(decodedBytes);
        if(fileType.equalsIgnoreCase("image/png") || fileType.equalsIgnoreCase("image/jpeg")){
            return true;
        }
        return false;
    }
}
