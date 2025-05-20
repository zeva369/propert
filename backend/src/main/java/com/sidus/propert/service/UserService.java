package com.sidus.propert.service;

import java.util.Optional;

import com.sidus.propert.dto.LoginRequestDTO;
import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.model.entity.User;

public interface UserService {
    Optional<UserDTO> findById(String id);
    Optional<UserDTO> findByUsername(String username);
    UserDTO create(UserInDTO user);
    Optional<UserDTO> validateCredentials(LoginRequestDTO loginRequestDTO);

}
