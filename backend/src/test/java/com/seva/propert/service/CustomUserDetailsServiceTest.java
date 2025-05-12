package com.seva.propert.service;


import com.seva.propert.model.entity.User;
import com.seva.propert.repository.UserRepository;
import com.seva.propert.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername Should Throw An Exception")
    void loadUserByUsernameShouldReturnUserNotFound() {
        String username = "nonexistentuser";
        // Mock the behavior of userRepository to return null when the user is not found
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Call the method and assert that an exception is thrown
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    @DisplayName("loadUserByUsername Should Return A Valid User")
    void loadUserByUsernameShouldReturnUser() {
        String username = "existinguser";
        // Mock the behavior of userRepository to return a user when found
        when(userRepository.findByUsername(username))
            .thenReturn(Optional.of(
                User.builder()
                    .username(username)
                    .password("password")
                    .role("USER")
                    .build()));

        UserDetails uDetails = customUserDetailsService.loadUserByUsername(username);
        assertNotNull(uDetails, "User should be found");
        assertEquals(username, uDetails.getUsername(), "Usernames should match");
    }
}
