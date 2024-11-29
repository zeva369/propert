package com.seva.propert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seva.propert.context.security.JWTHelper;
import com.seva.propert.model.entity.User;
import com.seva.propert.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;

    private final JWTHelper jwtHelper;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JWTHelper jwtHelper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User u) {
        return userService.findByUsername(u.getUsername())
                .map(user -> {
                    if (passwordEncoder.matches(u.getPassword(), user.getPassword())) {
                        String token = this.jwtHelper.generateToken(user.getUsername(), user.getRole());

                        // Configurar cookie
                        ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(jwtHelper.getMaxAgeFromToken(token))
                                .build();

                        return ResponseEntity.ok()
                                .header("Set-Cookie", cookie.toString())
                                .body("Login successful");
                    }
                    return ResponseEntity.status(401).body("Invalid credentials");
                })
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("Logout successful");
    }
}