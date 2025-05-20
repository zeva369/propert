package com.sidus.propert.service;

import com.sidus.propert.converter.UserConverter;
import com.sidus.propert.converter.UserInConverter;
import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.model.entity.User;
import com.sidus.propert.repository.UserRepository;
import com.sidus.propert.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

//    @InjectMocks
    private UserServiceImpl userService;

    private final String invalidUserId = "1234567890abcdef12345678";
    private final String validUserId = "abcde__xx07f12345678";
    private final String invalidUserName = "John Doe";
    private final String validUserName = "testuser";

    private final User mockUser = User.builder()
        .id(validUserId)
        .username(validUserName)
        .password("encodedPassword")
        .role("USER")
        .build();

    @BeforeEach
    void setUp() {
        GenericConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new UserInConverter());
        conversionService.addConverter(new UserConverter());

        // Crear instancia de UserServiceImpl con los beans y mocks
        userService = new UserServiceImpl(userRepository, passwordEncoder, conversionService);
    }

    @Test
    @DisplayName("findUserById should return Empty")
    void findUserByIdShouldReturnEmpty() throws Exception {
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        Optional<UserDTO> oUser = userService.findById(invalidUserId);
        Assertions.assertEquals(Optional.empty(), oUser);
    }

    @Test
    @DisplayName("findUserById should return a valid User object with the same ID")
    void findUserByIdShouldReturnUser() throws Exception {
        UserDTO u = new UserDTO(
            mockUser.getId(),
            mockUser.getUsername(),
            "USER");

        when(userRepository.findById(validUserId)).thenReturn(Optional.of(mockUser));

        Optional<UserDTO> oUser = userService.findById(validUserId);
        Assertions.assertTrue(oUser.isPresent());
        Assertions.assertEquals(u, oUser.get());
    }

    @Test
    @DisplayName("findUserByUserName should return Empty")
    void findUserByNameShouldReturnEmpty() throws Exception {
        when(userRepository.findByUsername(invalidUserName)).thenReturn(Optional.empty());

        Optional<UserDTO> oUser = userService.findByUsername(invalidUserName);
        Assertions.assertEquals(Optional.empty(), oUser);
    }

    @Test
    @DisplayName("findUserByUserName should return a valid User object with the same username")
    void findUserByNameShouldReturnUser()  {
        UserDTO u = new UserDTO(
                mockUser.getId(),
                mockUser.getUsername(),
                "USER");

        when(userRepository.findByUsername(validUserName)).thenReturn(Optional.of(mockUser));

        Optional<UserDTO> oUser = userService.findByUsername(validUserName);
        Assertions.assertTrue(oUser.isPresent());
        Assertions.assertEquals(u, oUser.get());
    }

    @Test
    @DisplayName("createUser should return a valid User object with the same attributes")
    void createUserShouldReturnAValidUser() throws Exception {
        User newUser = User.builder()
            .id(validUserId)
            .username(validUserName)
            .password("encodedPassword")
            .role("USER")
            .build();

        UserInDTO userInDTO = new UserInDTO(
            validUserId,
            validUserName,
            "abc123");

        when(userRepository.findById(validUserId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("abc123")).thenReturn("encodedPassword");
        when(userRepository.save(newUser)).thenReturn(newUser);

        UserDTO savedUser = userService.create(userInDTO);
        assertThat(savedUser.id()).isEqualTo(validUserId);
        assertThat(savedUser.username()).isEqualTo(validUserName);
        assertThat(savedUser.role()).isEqualTo("USER");
    }
}
