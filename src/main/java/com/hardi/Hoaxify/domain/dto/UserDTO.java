package com.hardi.Hoaxify.domain.dto;

import com.hardi.Hoaxify.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private long id;

    private String username;

    private String displayName;

    private String image;

    public UserDTO(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.image = user.getImage();
    }
}
