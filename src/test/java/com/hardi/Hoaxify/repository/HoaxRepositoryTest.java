package com.hardi.Hoaxify.repository;

import com.hardi.Hoaxify.domain.Hoax;
import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.utils.TestHoax;
import com.hardi.Hoaxify.utils.TestUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaRepositories
@ActiveProfiles("test")
public class HoaxRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    HoaxRepository hoaxRepository;

    @Test
    public void findHoaxesByUserWhenExistReturnHoaxes() {
        User user = testEntityManager.persist(TestUser.createCustomUser("user-1"));

        Hoax hoax = TestHoax.createHoax();
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        hoaxRepository.save(hoax);

        Page<Hoax> hoaxes = hoaxRepository.findByUser(user, PageRequest.of(0, 10));
        assertThat(hoaxes.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findHoaxesByUserWhenDoesNotExistReturnEmptyPage() {
        User user = TestUser.createCustomUser("unknown-user");
        testEntityManager.persist(user);
        Page<Hoax> hoaxes = hoaxRepository.findByUser(user, PageRequest.of(0, 10));
        assertThat(hoaxes.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void findHoaxesByUsernameWhenExistReturnHoaxes() {
        User user = testEntityManager.persist(TestUser.createCustomUser("user-1"));

        Hoax hoax = TestHoax.createHoax();
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        hoaxRepository.save(hoax);

        Page<Hoax> hoaxes = hoaxRepository.findByUserUsername("user-1", PageRequest.of(0, 10));
        assertThat(hoaxes.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findHoaxesByUsernameWhenDoesNotExistReturnEmpty() {
        Page<Hoax> hoaxes = hoaxRepository.findByUserUsername("unknown-user", PageRequest.of(0, 10));
        assertThat(hoaxes.getTotalElements()).isEqualTo(0);
    }


}
