package com.hardi.Hoaxify.service.file;

import com.hardi.Hoaxify.config.AppConfiguration;
import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.repository.FileAttachmentRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class FileServiceTest {

    FileService fileService;

    AppConfiguration appConfiguration;

    @MockBean
    FileAttachmentRepository fileAttachmentRepository;

    @BeforeEach
    public void init() {
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration, fileAttachmentRepository);

        new File(appConfiguration.getUploadPath()).mkdir();
        new File(appConfiguration.getFullProfileImagesPath()).mkdir();
        new File(appConfiguration.getFullAttachmentsPath()).mkdir();
    }

    @Test
    public void detectTypeWhenPngFileProvidedReturnImagePng() throws IOException {
        ClassPathResource resourceFile = new ClassPathResource("profile.png");
        byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
        String fileType = fileService.detectType(fileArr);
        assertThat(fileType).isEqualToIgnoringCase("image/png");
    }

    @Test
    public void cleanupStorageWhenOldFilesExistRemoveFilesFromStorage() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5L);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        File storedImage = new File(filePath);
        assertThat(storedImage.exists()).isFalse();
    }

    @Test
    public void cleanupStorageWhenOldFilesExistRemoveFilesFromDatabase() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5L);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        Mockito.verify(fileAttachmentRepository).deleteById(5L);
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }
}
