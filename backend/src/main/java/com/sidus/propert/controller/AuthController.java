package com.sidus.propert.controller;

import com.sidus.propert.dto.LoginRequestDTO;
import com.sidus.propert.service.JWTService;
import com.sidus.propert.service.TokenStoreService;
import com.sidus.propert.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenStoreService tokenStoreService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JWTService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.validateCredentials(loginRequestDTO)
                .map(user -> {
                    String accessToken = jwtService.generateAccessToken(user.id(), user.role());
                    String refreshToken = jwtService.generateRefreshToken(user.id(), user.role());

                    //Guardar el refresh token en redis
                    tokenStoreService.storeToken(user.id(), refreshToken);

                    // Configurar cookie para el refresh token
                    ResponseCookie refreshCookie  = ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(jwtService.getMaxAgeFromToken(refreshToken))
                            .build();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(Map.of("accessToken", accessToken));

                })
                .orElse(ResponseEntity.status(401).body(Map.of("message","Invalid credentials")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("Refresh token: {}", refreshToken);
        if (refreshToken == null || !jwtService.isValid(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtService.getUsernameFromToken(refreshToken);
        String userRole = jwtService.getUserRoleFromToken(refreshToken);
        if (!tokenStoreService.exists(userId)) {
            log.info("El usuario no existe o no tiene un refresh token válido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = jwtService.generateAccessToken(userId, userRole);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            String userId = jwtService.getUsernameFromToken(refreshToken);
            tokenStoreService.delete(userId); // opcional: elimina el refresh token del almacenamiento (Redis, etc.)
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful");
    }
}