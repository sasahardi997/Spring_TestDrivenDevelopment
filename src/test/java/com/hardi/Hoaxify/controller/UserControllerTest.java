package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.config.AppConfiguration;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.domain.dto.UserUpdateDTO;
import com.hardi.Hoaxify.exceptions.ApiError;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.UserService;
import com.hardi.Hoaxify.utils.GenericResponse;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hardi.Hoaxify.utils.TestUser.createValidUser;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //Default port 8080
@ActiveProfiles("test")
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @Test
    public void postUserWhenUserIsValidReceiveOk() {
        User user = createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUserWhenUserIsValidSaveToDatabase() {
        User user = createValidUser();
        postSignup(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUserWhenUserIsValidReceiveSuccessMessage() {
        User user = createValidUser();
        ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void postUserWhenUserIsValidPasswordIsHashedInDatabase() {
        User user = createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
        List<User> users = userRepository.findAll();
        User inDB = users.get(0);
        assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    public void postUserWhenUserHasNullUsernameReceiveBadRequest() {
        User user= createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasNullDisplayNameReceiveBadRequest() {
        User user= createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasNullPasswordReceiveBadRequest() {
        User user= createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasUserNameLessThen3CharactersReceiveBadRequest() {
        User user= createValidUser();
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasDisplayNameHasThen3CharactersReceiveBadRequest() {
        User user= createValidUser();
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasPasswordLessThen3CharactersReceiveBadRequest() {
        User user= createValidUser();
        user.setPassword("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasUserNameExceedsTheLengthLimitReceiveBadRequest() {
        User user= createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256)
                .mapToObj(x -> "a")
                .collect(Collectors.joining());
        user.setUsername(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserHasDisplayNameExceedsTheLengthLimitReceiveBadRequest() {
        User user= createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256)
                .mapToObj(x -> "a")
                .collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserPasswordExceedsTheLengthLimitReceiveBadRequest() {
        User user= createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256)
                .mapToObj(x -> "a")
                .collect(Collectors.joining());
        user.setPassword(valueOf256Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserPasswordWithAllLowercaseReceiveBadRequest() {
        User user= createValidUser();
        user.setPassword("allowlowercase");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserPasswordWithAllUppercaseReceiveBadRequest() {
        User user= createValidUser();
        user.setPassword("ALLOWUPPERCASE");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserPasswordWithAllNumberCaseReceiveBadRequest() {
        User user= createValidUser();
        user.setPassword("12345678");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUserWhenUserIsInvalidReceiveApiError() {
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    @Test
    public void postUserWhenUserIsInvalidReceiveApiErrorWithValidationErrors() {
        User user = new User();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    public void postUserWhenUserHasNullUsernameReceiverMessageOfNullErrorFoUsername() {
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
    }

    @Test
    public void postUserWhenUserHasUsernameWithSizeThreeReceiverMessageOfUsernameSize() {
        User user = createValidUser();
        user.setUsername("abc");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 6 and maximum 255 characters");
    }

    @Test
    public void postUserWhenUserHasNullPasswordReceiverMessageOfNullErrorForPassword() {
        User user = createValidUser();
        user.setPassword(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password cannot be null");
    }

    @Test
    public void postUserWhenUserHasPasswordWithLowerCasesReceiverMessageOfPasswordCasesRule() {
        User user = createValidUser();
        user.setPassword("abcabcabc");
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password must contain numbers, lower and upper cases");
    }

    @Test
    public void postUserWhenUserHasNullDisplayNameReceiveErrorMessage() {
        User user = createValidUser();
        user.setDisplayName(null);
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("displayName")).isEqualTo("Display name cannot be null");
    }

    @Test
    public void getUsersWhenThereAreNoUsersInDB_receiveOk() {
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUserWhenThereAreNoUserInDBReceivePageWithZeroItems() {
        //Cannot cast Object to page, so we need to implement exchange
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getUserWhenThereAreUserInDBReceivePageWithUser() {
        //Cannot cast Object to page, so we need to implement exchange
        userRepository.save(TestUser.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    public void getUserWhenThereAreUserInDBReceiveUserWithoutPassword() {
        //Cannot cast Object to Page, so we need to implement exchange
        userRepository.save(TestUser.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
        Map<String, Object> entity = response.getBody().getContent().get(0);
        assertThat(entity.containsKey("password")).isFalse();
    }

    @Test
    public void getUsersWhenPageIsRequestedFor3ItemsPerPage() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUser::createCustomUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?page=0&&size=3";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getContent().size()).isEqualTo(3);
    }

    @Test
    public void getUserWhenPageSizeIsReceivePageSizeAs10() {
        //Cannot cast Object to page, so we need to implement exchange
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void getUserWhenPageSizeIsGreaterThan100ReceivePageSizeAs100() {
        //Cannot cast Object to page, so we need to implement exchange
        String path = API_1_0_USERS + "?size=500";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getSize()).isEqualTo(100);
    }

    @Test
    public void getUserWhenPageSizeIsNegativeReceivePageSizeAs10() {
        //Cannot cast Object to page, so we need to implement exchange
        String path = API_1_0_USERS + "?size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void getUserWhenPageSizeIsNegativeReceiveFirstPage() {
        //Cannot cast Object to page, so we need to implement exchange
        String path = API_1_0_USERS + "?size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getNumber()).isEqualTo(0);
    }

    @Test
    public void getUsersWhenUserLoggedInReceivePageWithoutLoggedInUser() {
        userService.save(TestUser.createCustomUser("user-1"));
        userService.save(TestUser.createCustomUser("user-2"));
        userService.save(TestUser.createCustomUser("user-3"));
        authenticate("user-1");

        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    public void getUserByUsernameWhenUserExistReceiveOk() {
        String username = "test-user";
        userService.save(TestUser.createCustomUser(username));
        ResponseEntity<Object> response = getUser(username, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUserByUsernameWhenUserExistReceiveUserWithoutPassword() {
        String username = "test-user";
        userService.save(TestUser.createCustomUser(username));
        ResponseEntity<String> response = getUser(username, String.class);
        assertThat(response.getBody().contains("password")).isFalse();
    }

    @Test
    public void getUserByUsernameWhenUserDoesNotExistReceiveNotFound() {
        ResponseEntity<String> response = getUser("username-unknown", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getUserByUsernameWhenUserDoesNotExistReceiveApiError() {
        ResponseEntity<ApiError> response = getUser("username-unknown", ApiError.class);
        assertThat(response.getBody().getMessage().contains("username-unknown")).isTrue();
    }

    @Test
    public void putUserWhenUnauthorizedUserSendsTheRequestReceiveUnauthorized() {
        ResponseEntity<Object> response = putUser(123, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putUserWhenAuthorizedUserSendsUpdateFormAnotherUserReceiveForbidden() {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void putUserWhenUnauthorizedUserSendsTheRequestReceiveApiError() {
        ResponseEntity<ApiError> response = putUser(123, null, ApiError.class);
        assertThat(response.getBody().getUrl()).contains("users/123");
    }

    @Test
    public void putUserWhenAuthorizedUserSendsUpdateForAnotherUserReceiveApiError() {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);
        assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
    }

    @Test
    public void putUserWhenAuthorizedUserSendsValidRequestUserReceiveStatusOk() {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO("displayName");

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUserWhenAuthorizedUserDisplayNameUpdated() {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO("displayName");

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        putUser(user.getId(), requestEntity, Object.class);

        User userInDB = userRepository.findByUsername("user111").orElse(null);
        if(userInDB == null) return;
        assertThat(userInDB.getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    @Test
    public void putUserWhenAuthorizedUserDisplayNameReceiveUpdateUserDTO() {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO("displayName");

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateDTO> response = putUser(user.getId(), requestEntity, UserUpdateDTO.class);

        assertThat(response.getBody().getDisplayName()).isEqualTo(updatedUser.getDisplayName());
    }

    @Test
    public void putUserWithValidRequestBodyWithSupportedImageFromAuthorizedUserReceiveUserDTOWithRandomImageName() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateDTO> response = putUser(user.getId(), requestEntity, UserUpdateDTO.class);

        assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
    }

    @Test
    public void putUserWithValidRequestBodyWithSupportedImageFromAuthorizedUserImageIsStoredUnderProfileFolder() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateDTO> response = putUser(user.getId(), requestEntity, UserUpdateDTO.class);

        String storageImageName = response.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storageImageName;
        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void putUserWithInvalidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updateDTO);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUserWithInvalidRequestBodyWithSizeIssueDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setDisplayName("abc");

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updateDTO);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUserWithValidRequestBodyWithJPGImageFromAuthorizedUserReceiveOk() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateDTO> response = putUser(user.getId(), requestEntity, UserUpdateDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUserWithValidRequestBodyWithGIFImageFromAuthorizedUserBadRequest() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("test-gif.gif");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUserWithValidRequestBodyWithTXTImageFromAuthorizedUserReceiveValidationOnErrorForProfileImage() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("image")).isEqualTo("Only PNG and JPG files are allowed");
    }

    @Test
    public void putUserWithValidRequestBodyWithJPGImageFromAuthorizedUserWhoHasImageRemoveOldImageFromStorage() throws IOException {
        User user = userService.save(TestUser.createCustomUser("user111"));
        authenticate(user.getUsername());

        UserUpdateDTO updatedUser = TestUser.createUserUpdateDTO();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateDTO> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserUpdateDTO> response = putUser(user.getId(), requestEntity, UserUpdateDTO.class);

        putUser(user.getId(), requestEntity, UserUpdateDTO.class);

        String storageImageName = response.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storageImageName;
        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isFalse();
    }

    private String readFileToBase64(String fileName) throws IOException {
        ClassPathResource imageResource = new ClassPathResource(fileName);
        byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
        String imageString = Base64.getEncoder().encodeToString(imageArr);
        return imageString;
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUser(String username, Class<T> responseType){
        String path = API_1_0_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, responseType);
    }

    public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType){
        String path = API_1_0_USERS + "/" + id;
        //We need to use exchange, because put method doesn't have return type
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
    }

    @AfterEach
    public void cleanDirectories() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

//    @Test
//    @Disabled
//    public void postUserWhenUserHasSameUsernameReceiverBadRequest() {
//        userRepository.save(createValidUser());
//
//        User user = createValidUser();
//        ResponseEntity<Object> response = postSignup(user, Object.class);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @Disabled
//    public void postUserWhenUserHasSameUsernameReceiveMessage() {
//        userRepository.save(createValidUser());
//
//        User user = createValidUser();
//        ResponseEntity<DuplicateUsernameException> response = postSignup(user, DuplicateUsernameException.class);
//
//        assertThat(response.getBody().getMessage()).isEqualTo("User already exists");
//    }

}
