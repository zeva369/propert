package com.sidus.propert.service.impl;

import com.sidus.propert.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

    @Value("${jwt.secret.b64}")
    private String secretB64;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretB64));
    }

    @Override
    public String generateToken(String userName, String userRole, long expirationInMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userRole);

        return Jwts.builder().claims(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(key, Jwts.SIG.HS512).compact();
    }

    @Override
    public String generateAccessToken(String userName, String userRole) {
        // 15 min
        long ACCESS_TOKEN_EXP = 15 * 60 * 1000;
        return generateToken(userName, userRole, ACCESS_TOKEN_EXP);
    }

    @Override
    public String generateRefreshToken(String userName, String userRole) {
        // 7 días
        long REFRESH_TOKEN_EXP = 7 * 24 * 60 * 60 * 1000;
        return generateToken(userName, userRole, REFRESH_TOKEN_EXP);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @Override
    public String getUserRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("role"));
    }
    //retrieve expiration date from jwt token
    @Override
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Método para obtener el maxAge a partir de la fecha de expiración
    @Override
    public long getMaxAgeFromToken(String token) {
        // Obtenemos la fecha de expiración del token
        Date expirationDate = getExpirationDateFromToken(token);

        // Calculamos el tiempo en milisegundos hasta la expiración
        long diffInMillis = expirationDate.getTime() - System.currentTimeMillis();

        // Convertimos los milisegundos a segundos (maxAge debe estar en segundos)
        return diffInMillis / 1000; // maxAge en segundos
    }

    @Override
    public Boolean isValid(String token) {
        try {
            Jwts.parser()               // o parserBuilder() si quieres configurar más
                    .verifyWith(key)    // secretKey es un SecretKey, no un string
                    .build()
                    .parseSignedClaims(token); // valida firma y parsea payload (claims)

            return true;
        } catch (JwtException e) {
            // Firma inválida, token mal formado, expirado, etc.
            return false;
        }

    }

    //check if the token has expired
    @Override
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    @Override
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
