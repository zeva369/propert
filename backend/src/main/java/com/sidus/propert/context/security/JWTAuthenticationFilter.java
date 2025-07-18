package com.sidus.propert.context.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import com.sidus.propert.dto.UserDTO;
import com.sidus.propert.service.JWTService;
import com.sidus.propert.service.TokenStoreService;
import com.sidus.propert.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.security.core.context.SecurityContextHolder.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final String[] authWhitelist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Elimina "Bearer "

            if (!jwtService.isValid(token)) {
                authenticationEntryPoint.commence(request, response, new AuthenticationException("Token inválido") {});
                return;
            }

            String userId = jwtService.getUsernameFromToken(token);
            String userRole = jwtService.getUserRoleFromToken(token);

            // Crear UserDetails para Spring Security
            UserDetails userDetails = User.builder()
                    .username(userId)
                    .password("") // No necesitamos la contraseña aquí
                    .roles(userRole) // Roles del usuario
                    .build();

            // Configurar SecurityContext con el usuario autenticado
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            getContext().setAuthentication(authenticationToken);

        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
            AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(this.authWhitelist)
                 .anyMatch(whitelistedPattern -> pathMatcher.match(whitelistedPattern, path));
    }

}
