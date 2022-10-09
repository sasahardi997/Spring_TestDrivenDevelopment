package com.hardi.Hoaxify;

import com.hardi.Hoaxify.domain.User;
import com.hardi.Hoaxify.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.IntStream;

@SpringBootApplication
public class HoaxifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoaxifyApplication.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner run(UserService userService) {
		return args -> IntStream.rangeClosed(1, 15)
				.mapToObj(i -> {
					User user = new User();
					user.setUsername("user-" + i);
					user.setDisplayName("test-display");
					user.setPassword("P4ssword");
					return user;
				})
				.forEach(userService::save);
	}

}
