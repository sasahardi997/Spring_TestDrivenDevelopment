package com.hardi.Hoaxify.repository;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.utils.TestUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void findByUsernameWhenUserExistsReturnUser() {
        testEntityManager.persist(TestUser.createValidUser());
        Optional<User> inDB = userRepository.findByUsername("test-user");
        assertThat(inDB).isPresent();
    }

    @Test
    public void findByUsernameWhenUserDoesNotExistsReturnNull() {
        Optional<User> inDB = userRepository.findByUsername("noneexistinguser");
        assertThat(inDB).isNotPresent();
    }
}
