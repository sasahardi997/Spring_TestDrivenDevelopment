package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.config.AppConfiguration;
import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.repository.FileAttachmentRepository;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.UserService;
import com.hardi.Hoaxify.utils.TestUser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

    public static final String API_1_0_HOAXES_UPLOAD = "/api/1.0/hoaxes/upload";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @BeforeEach
    public void init() throws IOException {
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        ResponseEntity<Object> response = uploadFile(requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void uploadFileWithImageFromUnauthorizedUserReceiveUnauthorized() {
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserReceiveFileAttachmentWithDate() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        ResponseEntity<FileAttachment> response = uploadFile(requestEntity, FileAttachment.class);
        assertThat(response.getBody().getDate()).isNotNull();
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserReceiveFileAttachmentWithRandomName() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        ResponseEntity<FileAttachment> response = uploadFile(requestEntity, FileAttachment.class);
        assertThat(response.getBody().getName()).isNotNull();
        assertThat(response.getBody().getName()).isNotEqualTo("profile.png");
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserImageSavedToFiled() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        ResponseEntity<FileAttachment> response = uploadFile(requestEntity, FileAttachment.class);
        String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + response.getBody().getName();
        File storedImage = new File(imagePath);
        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserFileAttachmentSavedToDatabase() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        uploadFile(requestEntity, FileAttachment.class);
        assertThat(fileAttachmentRepository.count()).isEqualTo(1);
    }

    @Test
    public void uploadFileWithImageFromAuthorizedUserFileAttachmentSavedToDatabaseWithFileType() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getRequestEntity();
        uploadFile(requestEntity, FileAttachment.class);
        FileAttachment storedFile = fileAttachmentRepository.findAll().get(0);
        assertThat(storedFile.getFileType()).isEqualTo("image/png");
    }

    public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES_UPLOAD, HttpMethod.POST, requestEntity, responseType);
    }

    private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
