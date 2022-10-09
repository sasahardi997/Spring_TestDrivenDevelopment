package com.hardi.Hoaxify.controller;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.exceptions.ApiError;
import com.hardi.Hoaxify.repository.UserRepository;
import com.hardi.Hoaxify.service.UserService;
import com.hardi.Hoaxify.utils.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

    private static final String API_1_0_LOGIN = "/api/1.0/login";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

//    @Test
//    @Disabled
//    public void postLoginWithoutUserCredentialsReceiveUnauthorized() {
//        ResponseEntity<Object> response = login(Object.class);
//        //TODO GETTING 403 INSTEAD OF 401 (TRY TO FIX LATER)
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
//    }

    @Test
    public void postLoginWithIncorrectUserCredentialsReceiveUnauthorized() {
        authenticate();
        ResponseEntity<Object> response = login(Object.class);
        //TODO GETTING 403 INSTEAD OF 401 (TRY TO FIX LATER)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postLoginWithoutUserCredentialsReceiveApiError() {
        ResponseEntity<ApiError> response = login(ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
    }

    @Test
    public void postLoginWithoutUserCredentialsReceiveApiErrorWithoutValidationErrors() {
        ResponseEntity<String> response = login(String.class);
        assertThat(response.getBody().contains("validationErrors")).isFalse();
    }

    @Test
    public void postLoginWithIncorrectUserCredentialsReceiveUnauthorizedWithoutWWWAuthenticationHeader() {
        authenticate();
        ResponseEntity<Object> response = login(Object.class);
        assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
    }

    @Test
    public void postLoginWithValidCredentialsReceiveOk() {
        userService.save(TestUser.createValidUser());
        authenticate();
        ResponseEntity<Object> response  = login(Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postLoginWithValidCredentialsReceiveLoggedInUserId() {
        User idDB = userService.save(TestUser.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response  = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        Integer id = (Integer) body.get("id");
        assertThat(id).isEqualTo(idDB.getId().intValue());
    }

    @Test
    public void postLoginWithValidCredentialsReceiveLoggedInUserImage() {
        User idDB = userService.save(TestUser.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response  = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        String image = (String) body.get("image");
        assertThat(image).isEqualTo(idDB.getImage());
    }

    @Test
    public void postLoginWithValidCredentialsReceiveLoggedInUserDisplayName() {
        User idDB = userService.save(TestUser.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response  = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        String displayName = (String) body.get("displayName");
        assertThat(displayName).isEqualTo(idDB.getDisplayName());
    }

    @Test
    public void postLoginWithValidCredentialsNotReceiveLoggedInUserPassword() {
        User idDB = userService.save(TestUser.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response  = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        assertThat(body.containsKey("password")).isFalse();
    }

    private void authenticate() {
        testRestTemplate.getRestTemplate().getInterceptors()
                .add(new BasicAuthenticationInterceptor("test-user", "P4ssword"));
    }

    public <T> ResponseEntity<T> login(Class<T> responseType){
        return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
    }

    //For HashMap and others generic types
    public <T> ResponseEntity<T> login(ParameterizedTypeReference<T> responseType){
        return testRestTemplate.exchange(API_1_0_LOGIN, HttpMethod.POST, null, responseType);
    }
}
