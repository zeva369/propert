package com.seva.propert.service;

import com.seva.propert.model.entity.User;
import com.seva.propert.repository.UserRepository;
import com.seva.propert.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private final String invalidUserId = "1234567890abcdef12345678";
    private final String validUserId = "abcde__xx07f12345678";
    private final String invalidUserName = "John Doe";
    private final String validUserName = "testuser";

    private User mockUser;

    UserServiceImplTest() {
        mockUser = new User();
        mockUser.setId(validUserId);
        mockUser.setUsername(validUserName);
        mockUser.setPassword("abc123");
        mockUser.setRole("USER");
    }

    @Test
    @DisplayName("findUserById should return Empty")
    void findUserByIdShouldReturnEmpty() throws Exception {
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        Optional<User> oUser = userService.findById(invalidUserId);
        Assertions.assertEquals(Optional.empty(), oUser);
    }

    @Test
    @DisplayName("findUserById should return a valid User object with the same ID")
    void findUserByIdShouldReturnUser() throws Exception {
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(mockUser));

        Optional<User> oUser = userService.findById(validUserId);
        Assertions.assertTrue(oUser.isPresent());
        Assertions.assertEquals(mockUser, oUser.get());
    }

    @Test
    @DisplayName("findUserByUserName should return Empty")
    void findUserByNameShouldReturnEmpty() throws Exception {
        when(userRepository.findByUsername(invalidUserName)).thenReturn(Optional.empty());

        Optional<User> oUser = userService.findByUsername(invalidUserName);
        Assertions.assertEquals(Optional.empty(), oUser);
    }

    @Test
    @DisplayName("findUserByUserName should return a valid User object with the same username")
    void findUserByNameShouldReturnUser() throws Exception {
        when(userRepository.findByUsername(validUserName)).thenReturn(Optional.of(mockUser));

        Optional<User> oUser = userService.findByUsername(validUserName);
        Assertions.assertTrue(oUser.isPresent());
        Assertions.assertEquals(mockUser, oUser.get());
    }

    @Test
    @DisplayName("createUser should return a valid User object with the same attributes")
    void createUserShouldReturnAValidUser() throws Exception {
        User newUser = new User();
        newUser.setId(validUserId);
        newUser.setUsername(validUserName);
        newUser.setPassword("abc123");
        newUser.setRole("USER");

        when(userRepository.save(newUser)).thenReturn(newUser);

        User savedUser = userService.create(newUser);

        Assertions.assertEquals(savedUser, newUser);
    }
}
