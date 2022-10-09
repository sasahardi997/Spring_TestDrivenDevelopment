package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.config.AppConfiguration;
import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.HoaxDTO;
import com.hardi.Hoaxify.exceptions.ApiError;
import com.hardi.Hoaxify.repository.FileAttachmentRepository;
import com.hardi.Hoaxify.repository.HoaxRepository;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.HoaxService;
import com.hardi.Hoaxify.service.UserService;
import com.hardi.Hoaxify.service.file.FileService;
import com.hardi.Hoaxify.utils.GenericResponse;
import com.hardi.Hoaxify.utils.TestHoax;
import com.hardi.Hoaxify.utils.TestPage;
import com.hardi.Hoaxify.utils.TestUser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //Default port - 8080
@ActiveProfiles("test")
public class HoaxControllerTest {

    private static final String API_1_0_HOAXES = "/api/1.0/hoaxes";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @Autowired
    HoaxService hoaxService;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsUnauthorizedReceiveUnauthorized() {
        Hoax hoax = TestHoax.createHoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsUnauthorizedReceiveApiError() {
        Hoax hoax = TestHoax.createHoax();
        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedHoaxSavedToDatabase() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        postHoax(hoax, Object.class);
        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedHoaxSavedToDatabaseWithTimestamp() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        postHoax(hoax, Object.class);
        Hoax inDB = hoaxRepository.findAll().get(0);
        assertThat(inDB.getTimestamp()).isNotNull();
    }

    @Test
    public void postHoaxWhenHoaxContentIsNullAndUserIsAuthorizedReceiveBadRequest() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = new Hoax();
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoaxWhenHoaxContentIsLessThan10CharactersAndUserIsAuthorizedReceiveBadRequest() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = new Hoax();
        hoax.setContent("123456789");
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoaxWhenHoaxContentIs5000CharactersAndUserIsAuthorizedReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = new Hoax();
        String longString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(longString);
        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoaxWhenHoaxContentIs5001CharactersAndUserIsAuthorizedReceiveBadRequest() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        Hoax hoax = new Hoax();
        String longString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        hoax.setContent(longString);

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoaxWhenHoaxContentIsNullAndUserIsAuthorizedReceiveApiError() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        Hoax hoax = new Hoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("content")).isNotNull();
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedHoaxSavedToDatabaseWithUserInfo() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        postHoax(hoax, Object.class);
        Hoax inDB = hoaxRepository.findAll().get(0);
        assertThat(inDB.getUser().getUsername()).isEqualTo("user-1");
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedHoaxCanBeAccessedFromUserEntity() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        postHoax(hoax, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        User inDBUser = entityManager.find(User.class, user.getId());

        assertThat(inDBUser.getHoaxes().size()).isEqualTo(1);
    }

    @Test
    public void getHoaxesWhenThereAreNoHoaxesReceiveOk() {
        ResponseEntity<Object> response = getHoaxes(new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxesWhenThereAreNoHoaxesReceivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxesWhenThereAreNoHoaxesReceivePageWithItems() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxesWhenThereAreNoHoaxesReceiveHoaxWithHoaxDTO() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<HoaxDTO>> response = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxDTO>>() {});
        HoaxDTO dto = response.getBody().getContent().get(0);
        assertThat(dto.getUser().getUsername()).isEqualTo("user-1");
    }

    @Test
    public void postHoaxWhenHoaxIsValidAndUserIsAuthorizedReceiveHoxDTO() {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        Hoax hoax = TestHoax.createHoax();
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user-1");
    }

    @Test
    public void postHoaxWhenHoaxHasFileAttachmentIsValidAndUserIsAuthorizedReceiveHoxDTO() throws IOException {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestHoax.createHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        assertThat(inDB.getHoax().getId()).isEqualTo(response.getBody().getId());
    }

    @Test
    public void postHoaxWhenHoaxHasFileAttachmentIsValidAndUserIsAuthorizedHoaxFileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestHoax.createHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);

        Hoax inDB = hoaxRepository.findById(response.getBody().getId()).get();
        assertThat(inDB.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postHoaxWhenHoaxHasFileAttachmentIsValidAndUserIsAuthorizedReceiveHoaxDTOWithAttachment() throws IOException {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestHoax.createHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);

        assertThat(response.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    @Test
    public void getHoaxesOfUserWhenUserExistReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        ResponseEntity<Object> response = getHoaxesOfUser("user-1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxesOfUserWhenUserDoesNotExistReceiveNotFound() {
        ResponseEntity<Object> response = getHoaxesOfUser("unknown", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getHoaxesOfUserWhenUserExistReceivePageWithZeroHoaxes() {
        userService.save(TestUser.createCustomUser("user-1"));
        ResponseEntity<TestPage<Object>> response = getHoaxesOfUser("user-1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxesWhenUserExistWithHoaxesReceivePageWithHoaxDTO() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<HoaxDTO>> response = getHoaxesOfUser("user-1", new ParameterizedTypeReference<TestPage<HoaxDTO>>() {});
        HoaxDTO dto = response.getBody().getContent().get(0);
        assertThat(dto.getUser().getUsername()).isEqualTo("user-1");
    }

    @Test
    public void getHoaxesWhenUserExistWithMultipleHoaxesReceivePageWithHoaxDTO() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<HoaxDTO>> response = getHoaxesOfUser("user-1", new ParameterizedTypeReference<TestPage<HoaxDTO>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxesWhenMultipleUserExistWithMultipleHoaxesReceivePageWithMatchingHoaxesCount() {
        User userWithThree = userService.save(TestUser.createCustomUser("user-1"));
        User userWithFive = userService.save(TestUser.createCustomUser("user-2"));

        IntStream.rangeClosed(1,3).forEach(i -> {
            hoaxService.save(userWithThree, TestHoax.createHoax());
        });

        IntStream.rangeClosed(1,5).forEach(i -> {
            hoaxService.save(userWithFive, TestHoax.createHoax());
        });

        ResponseEntity<TestPage<HoaxDTO>> response = getHoaxesOfUser("user-2", new ParameterizedTypeReference<TestPage<HoaxDTO>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getOldHoaxesWhenThereAreNoHoaxesReceiveOk() {
        ResponseEntity<Object> response = getOldHoaxes(5, new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxesWhenThereHoaxesReceivePageWithItemsProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<Object>> response = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxesOfUserWhenThereAreNoHoaxesReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user-1", new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxesOfUserWhenThereHoaxesReceivePageWithItemsProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(fourth.getId(), "user-1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxesOfUserWhenUserDoesNotExistWhenThereAreNoHoaxesReceiveNotFound() {
        ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user-1", new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getOldHoaxesOfUserWhenThereAreNoHoaxesReceivePageWithZeroItems() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        userService.save(TestUser.createCustomUser("user-2"));

        ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(fourth.getId(), "user-2", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewHoaxesWhenThereHoaxesReceivePageWithItemsAfterProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<List<Object>> response = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxesOfUserWhenThereHoaxesReceivePageWithItemsAfterProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<List<Object>> response = getNewHoaxesOfUser(fourth.getId(), "user-1", new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxesOfUserWhenThereAreNoHoaxesReceiveOk() {
        userService.save(TestUser.createCustomUser("user-1"));
        ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user-1", new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewHoaxesOfUserWhenUserDoesNotExistWhenThereAreNoHoaxesReceiveNotFound() {
        ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user-1", new ParameterizedTypeReference<Object>() { });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getNewHoaxesOfUserWhenThereAreNoHoaxesReceivePageWithZeroItems() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        userService.save(TestUser.createCustomUser("user-2"));

        ResponseEntity<List<Object>> response = getNewHoaxesOfUser(fourth.getId(), "user-2", new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewHoaxesCountWhenThereHoaxesReceiveCountAfterProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<Map<String, Long>> response = getNewHoaxesCount(fourth.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getNewUserHoaxesCountWhenThereHoaxesReceiveCountAfterProvidedId() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        HoaxDTO fourth = hoaxService.save(user, TestHoax.createHoax());
        hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<Map<String, Long>> response = getNewHoaxesOfUserCount(fourth.getId(), "user-1", new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void deleteHoaxWhenUserIsUnauthorizedReceiveUnauthorized() {
        ResponseEntity<Object> response = deleteHoax(555, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deleteHoaxWhenUserIsAuthorizedReceiveOk() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        HoaxDTO hoaxDto = hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<Object> response = deleteHoax(hoaxDto.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteHoaxWhenUserIsAuthorizedReceiveGenericResponse() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        HoaxDTO hoaxDto = hoaxService.save(user, TestHoax.createHoax());

        ResponseEntity<GenericResponse> response = deleteHoax(hoaxDto.getId(), GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void deleteHoaxWhenUserIsAuthorizedHoaxRemovedFromDatabase() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        HoaxDTO hoaxDto = hoaxService.save(user, TestHoax.createHoax());

        deleteHoax(hoaxDto.getId(), Object.class);
        Optional<Hoax> inDB = hoaxRepository.findById(hoaxDto.getId());
        assertThat(inDB.isPresent()).isFalse();
    }

    @Test
    public void deleteHoaxWhenHoaxIsOwnedByAnotherUserReceiveForbidden() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        User hoaxOwner = userService.save(TestUser.createCustomUser("hoax-owner"));
        HoaxDTO hoaxDto = hoaxService.save(hoaxOwner, TestHoax.createHoax());

        ResponseEntity<Object> response = deleteHoax(hoaxDto.getId(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteHoaxWhenHoaxNotExistReceiveForbidden() {
        User user = userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");
        ResponseEntity<Object> response = deleteHoax(55, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteHoaxWhenHoaxHasAttachmentAttachmentRemovedFromDatabase() throws IOException {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestHoax.createHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);

        long hoaxId = response.getBody().getId();

        deleteHoax(hoaxId, Object.class);
        Optional<FileAttachment> optionalFileAttachment = fileAttachmentRepository.findById(savedFile.getId());
        assertThat(optionalFileAttachment.isPresent()).isFalse();
    }

    @Test
    public void deleteHoaxWhenHoaxHasAttachmentAttachmentRemovedFromStorage() throws IOException {
        userService.save(TestUser.createCustomUser("user-1"));
        authenticate("user-1");

        MultipartFile file = createFile();
        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestHoax.createHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxDTO> response = postHoax(hoax, HoaxDTO.class);

        long hoaxId = response.getBody().getId();

        deleteHoax(hoaxId, Object.class);
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
        File storedImage = new File(attachmentFolderPath);
        assertThat(storedImage.exists()).isFalse();
    }

    public <T> ResponseEntity<T> deleteHoax(long hoaxId, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES + "/" + hoaxId, HttpMethod.DELETE, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxesOfUserCount(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxesCount(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    private <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());
        return new MockMultipartFile("profile.png", fileAsByte);
    }

    @AfterEach
    public void cleanupAfter(){
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
    }
}
