package com.sidus.propert.service.impl;

import java.util.Optional;

import com.sidus.propert.dto.LoginRequestDTO;
import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.exception.InvalidUserIdException;
import com.sidus.propert.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sidus.propert.repository.UserRepository;
import com.sidus.propert.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final ConversionService conversionService;

    @Override
    public Optional<UserDTO> findById(String id) {
        return repo.findById(id)
                .map(user -> conversionService.convert(user, UserDTO.class));
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return repo.findByUsername(username)
                .map(user -> conversionService.convert(user, UserDTO.class));
    }

    @Override
    public UserDTO create(UserInDTO userIn) {
        // Check if the user already exists
        if (findById(userIn.id()).isPresent()) {
            throw new InvalidUserIdException();
        }
        User newUser = conversionService.convert(userIn, User.class);
        newUser.setPassword(passwordEncoder.encode(userIn.password()));
        newUser.setRole("USER");
        return conversionService.convert(repo.save(newUser), UserDTO.class);
    }

    @Override
    public Optional<UserDTO> validateCredentials(LoginRequestDTO loginRequestDTO) {
        return repo.findById(loginRequestDTO.id())
                .filter(user -> passwordEncoder.matches(loginRequestDTO.password(), user.getPassword()))
                .map(user -> conversionService.convert(user, UserDTO.class));
    }


}
