package com.sidus.propert.controller;
import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.dto.UserInDTO;
import com.sidus.propert.service.UserService;
import jakarta.validation.Valid;
import com.sidus.propert.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Register a new user
    @PostMapping
    public ResponseEntity<UserDTO> createUsers(@Valid @RequestBody UserInDTO userInDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(userService.create(userInDTO));
    }
}