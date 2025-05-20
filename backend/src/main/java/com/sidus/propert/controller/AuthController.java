package com.sidus.propert.controller;

import com.sidus.propert.dto.LoginRequestDTO;
import com.sidus.propert.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.sidus.propert.context.security.JWTHelper;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JWTHelper jwtHelper;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.validateCredentials(loginRequestDTO)
                .map(user -> {
                        String token = this.jwtHelper.generateToken(user.id(), user.role());

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

                })
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
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