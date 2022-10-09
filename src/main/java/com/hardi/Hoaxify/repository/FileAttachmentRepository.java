package com.hardi.Hoaxify.repository;

import com.hardi.Hoaxify.domain.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByDateBeforeAndHoaxIsNull(Date date);
}
