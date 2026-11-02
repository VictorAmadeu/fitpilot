
package com.fitpilot.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {

        User user = new User(
                "test@fitpilot.example",
                "hash-ficticio-para-test"
        );

        User savedUser = userRepository.saveAndFlush(user);

        Optional<User> foundUser =
                userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());

        assertEquals(
                "test@fitpilot.example",
                foundUser.get().getEmail()
        );

        assertEquals(
                UserRole.USER,
                foundUser.get().getRole()
        );

        assertEquals(
                UserStatus.ACTIVE,
                foundUser.get().getStatus()
        );

        assertNotNull(foundUser.get().getCreatedAt());
        assertNotNull(foundUser.get().getUpdatedAt());
    }

    @Test
    void shouldFindUserByEmailIgnoringCase() {

        User user = new User(
                "victor@fitpilot.example",
                "hash-ficticio-para-test"
        );

        userRepository.saveAndFlush(user);

        Optional<User> foundUser =
                userRepository.findByEmailIgnoreCase(
                        "VICTOR@FITPILOT.EXAMPLE"
                );

        assertTrue(foundUser.isPresent());

        assertTrue(
                userRepository.existsByEmailIgnoreCase(
                        "Victor@Fitpilot.Example"
                )
        );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {

        User firstUser = new User(
                "duplicado@fitpilot.example",
                "hash-ficticio-uno"
        );

        User secondUser = new User(
                "DUPLICADO@FITPILOT.EXAMPLE",
                "hash-ficticio-dos"
        );

        userRepository.saveAndFlush(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }
}
