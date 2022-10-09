package com.hardi.Hoaxify;

import com.hardi.Hoaxify.config.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void checkStaticFolderWhenAppIsInitializedUploadFolderMustExist() {
        File uploadFolder = new File(appConfiguration.getUploadPath());
        boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
        assertThat(uploadFolderExist).isTrue();
    }

    @Test
    public void checkStaticFolderWhenAppIsInitializedProfileImageSubFolderMustExist() {
        String profileImageFolderPath = appConfiguration.getFullProfileImagesPath();

        File profileImageFolder = new File(profileImageFolderPath);
        boolean profileImageFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();
        assertThat(profileImageFolderExist).isTrue();
    }

    @Test
    public void checkStaticFolderWhenAppIsInitializedAttachmentsSubFolderMustExist() {
        String attachmentsFolderPath = appConfiguration.getFullAttachmentsPath();

        File attachmentsFolder = new File(attachmentsFolderPath);
        boolean attachmentsFolderExist = attachmentsFolder.exists() && attachmentsFolder.isDirectory();
        assertThat(attachmentsFolderExist).isTrue();
    }

    @Test
    public void getStaticFileWhenImageExistInProfileUploadFolderReceiveOk() throws Exception {
        String fileName = "profile-picture.png";

        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + fileName);
        FileUtils.copyFile(source, target);

        mockMvc.perform(MockMvcRequestBuilders.get("/images/" + appConfiguration.getProfileImagesFolder() + "/" + fileName))
                .andExpect(status().isOk());
    }

    @Test
    public void getStaticFileWhenImageExistInAttachmentsFolderReceiveOk() throws Exception {
        String fileName = "profile-picture.png";

        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + fileName);
        FileUtils.copyFile(source, target);

        mockMvc.perform(MockMvcRequestBuilders.get("/images/" + appConfiguration.getAttachmentsFolder() + "/" + fileName))
                .andExpect(status().isOk());
    }

    @Test
    public void getStaticFileWhenImageDoesNotExistReceiveNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/images/" + appConfiguration.getProfileImagesFolder() + "/there-is-no-such-image.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test does browser caching")
    public void getStaticFileWhenImageExistInAttachmentsFolderReceiveOkWithCacheHeaders() throws Exception {
        String fileName = "profile-picture.png";

        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + fileName);
        FileUtils.copyFile(source, target);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/images/" + appConfiguration.getAttachmentsFolder() + "/" + fileName))
                .andReturn();
        String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
        assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }
}
