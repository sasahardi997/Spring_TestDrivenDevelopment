package com.hardi.Hoaxify.repository;

import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.utils.TestHoax;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Test
    public void findByDateBeforeAndHoaxIsNullWhenAttachmentsDateOlderThanOneHourReturnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNullWhenAttachmentsDateOlderThanOneHourButHaveHoaxReturnsNone() {
        Hoax hoax1 = testEntityManager.persist(TestHoax.createHoax());
        Hoax hoax2 = testEntityManager.persist(TestHoax.createHoax());
        Hoax hoax3 = testEntityManager.persist(TestHoax.createHoax());

        testEntityManager.persist(getOneHourOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOneHourOldFileAttachmentWithHoax(hoax2));
        testEntityManager.persist(getOneHourOldFileAttachmentWithHoax(hoax3));

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNullWhenAttachmentsDateWithinOneHourReturnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHours());
        testEntityManager.persist(getFileAttachmentWithinOneHours());
        testEntityManager.persist(getFileAttachmentWithinOneHours());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNullWhenAttachmentsIsRelative() {
        Hoax hoax1 = testEntityManager.persist(TestHoax.createHoax());
        testEntityManager.persist(getOneHourOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHours());


        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(1);
    }

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60*60*1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getFileAttachmentWithinOneHours() {
        Date date = new Date(System.currentTimeMillis() - (60*1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOneHourOldFileAttachmentWithHoax(Hoax hoax) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setHoax(hoax);
        return fileAttachment;
    }
}
