package com.hardi.Hoaxify.domain.dto;

import com.hardi.Hoaxify.utils.ProfileImage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateDTO {

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @ProfileImage
    private String image;
}
