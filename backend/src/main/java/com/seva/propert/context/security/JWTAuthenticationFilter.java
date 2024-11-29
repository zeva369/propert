package com.seva.propert.context.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.seva.propert.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTHelper jwtHelper;
    private final UserService userService; // Para cargar usuarios desde la base de datos
    private final String[] authWhitelist;

    public JWTAuthenticationFilter(JWTHelper jwtHelper, UserService userService, String[] authWhitelist) {
        this.jwtHelper = jwtHelper;
        this.userService = userService;
        this.authWhitelist = authWhitelist;
    }   
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug(">>>Entra al filtro");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extraer el token JWT de las cookies
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .findFirst();

            if (jwtCookie.isPresent()) {
                String token = jwtCookie.get().getValue();

                try {
                    // Validar el token utilizando JWTHelper
                    if (!jwtHelper.isTokenExpired(token)) {
                        String username = jwtHelper.getUsernameFromToken(token);

                        // Cargar usuario desde la base de datos usando UserService
                        Optional<com.seva.propert.model.entity.User> userOptional = userService.findByUsername(username);

                        if (userOptional.isPresent()) {
                            com.seva.propert.model.entity.User user = userOptional.get();

                            // Crear UserDetails para Spring Security
                            UserDetails userDetails = User.builder()
                                    .username(user.getUsername())
                                    .password("") // No necesitamos la contraseña aquí
                                    .roles(user.getRole()) // Roles del usuario
                                    .build();

                            // Configurar SecurityContext con el usuario autenticado
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Manejo de errores si el token no es válido o hay problemas
                    System.err.println("JWTAuthenticationFilter: Token inválido o error en la validación: " + e.getMessage());
                }
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.debug(">>>Entra a shouldNotFilter :" + request.getRequestURI());
        String path = request.getRequestURI();
            AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(this.authWhitelist)
                 .anyMatch(whitelistedPattern -> pathMatcher.match(whitelistedPattern, path));
    }

    // @Override
    // protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    //         throws IOException, ServletException {

    //     Cookie[] cookies = request.getCookies();
    //     if (cookies != null) {
    //         Optional<Cookie> jwtCookie = Arrays.stream(cookies).filter(c -> "jwtToken".equals(c.getName())).findFirst();
    //         if (jwtCookie.isPresent() && jwtHelper.validateToken(jwtCookie.get().getValue())) {
    //             Claims claims = jwtHelper.getClaimsFromToken(jwtCookie.get().getValue());

    //             // Configurar la autenticación en el contexto de seguridad
    //             User authUser = new User(claims.getSubject(), "", List.of(() -> claims.get("role").toString()));
    //             SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities()));
    //         }
    //     }

    //     chain.doFilter(request, response);
    // }

    // @Override
    // protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    //         throws ServletException, IOException {
    //     Cookie[] cookies = request.getCookies();
    //     if (cookies != null) {
    //         // Busca el JWT en las cookies
    //         Cookie jwtCookie = Arrays.stream(cookies)
    //                 .filter(cookie -> "jwtToken".equals(cookie.getName()))
    //                 .findFirst()
    //                 .orElse(null);

    //         if (jwtCookie != null) {
    //             String token = jwtCookie.getValue();
    //             try {
    //                 // Valida el token
    //                 Claims claims = Jwts.parser()
    //                         .setSigningKey(SECRET_KEY)
    //                         .parseClaimsJws(token)
    //                         .getBody();

    //                 // Puedes agregar lógica adicional aquí, como almacenar datos del usuario
    //                 request.setAttribute("username", claims.getSubject());
    //             } catch (Exception e) {
    //                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    //                 return;
    //             }
    //         }
    //     }

    //     filterChain.doFilter(request, response);
    // }

    // @Override
    // protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    //     // Authorization=Bearer <token>

    //     String requestHeader = request.getHeader("Authorization");
    //     String username = null;
    //     String token = null;

    //     logger.info("JWTAuthenticationFilter -> Request from: " + request.getRemoteHost());
    //     logger.info(" Header :  {}", requestHeader);
        
    //     if (requestHeader != null && requestHeader.startsWith("Bearer")) {
    //         //looking good
    //         token = requestHeader.substring(7);
    //         try {
    //             username = this.jwtHelper.getUsernameFromToken(token);
    //         } catch (IllegalArgumentException e) {
    //             logger.info("Illegal Argument while fetching the username !!");
    //             e.printStackTrace();
    //         } catch (ExpiredJwtException e) {
    //             logger.info("Given jwt token is expired !!");
    //             e.printStackTrace();
    //         } catch (MalformedJwtException e) {
    //             logger.info("Some changed has done in token !! Invalid Token");
    //             e.printStackTrace();
    //         } catch (SignatureException e) {
    //         	logger.info("Signature isn't computed locally");
    //             e.printStackTrace();
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //     } else {
    //         logger.info("Invalid Header Value !! ");
    //     }

    //     if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    //         //fetch user detail from username
    //         UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
    //         Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
    //         if (validateToken) {
    //             //set the authentication
    //             UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    //             authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    //             SecurityContextHolder.getContext().setAuthentication(authentication);
    //         } else {
    //             logger.info("Validation fails !!");
    //         }
    //     }
    //     filterChain.doFilter(request, response);
    // }
}
