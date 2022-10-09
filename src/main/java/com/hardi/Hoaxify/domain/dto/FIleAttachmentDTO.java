package com.hardi.Hoaxify.domain.dto;

import com.hardi.Hoaxify.domain.FileAttachment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FIleAttachmentDTO {

    private String name;

    private String fileType;

    public FIleAttachmentDTO(FileAttachment fileAttachment) {
        this.setName(fileAttachment.getName());
        this.setFileType(fileAttachment.getFileType());
    }
}
