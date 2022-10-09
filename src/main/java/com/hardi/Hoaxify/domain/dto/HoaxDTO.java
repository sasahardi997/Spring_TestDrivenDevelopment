package com.hardi.Hoaxify.domain.dto;

import com.hardi.Hoaxify.domain.Hoax;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaxDTO {

    private Long id;

    private String content;

    private Long date;

    private UserDTO user;

    private FIleAttachmentDTO attachment;

    public HoaxDTO(Hoax hoax) {
        this.setId(hoax.getId());
        this.setContent(hoax.getContent());
        this.setDate(hoax.getTimestamp().getTime());
        this.setUser(new UserDTO(hoax.getUser()));
        if(hoax.getAttachment() != null) {
            this.setAttachment(new FIleAttachmentDTO(hoax.getAttachment()));
        }
    }
}
