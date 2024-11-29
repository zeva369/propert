package com.seva.propert.controller;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seva.propert.model.entity.User;
import com.seva.propert.service.UserService;

@RestController
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Endpoint para crear usuarios
    @PostMapping
    public String createUsers(@RequestBody User user) {
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.create(user);
        return "Usuarios creados con éxito";
    }
}