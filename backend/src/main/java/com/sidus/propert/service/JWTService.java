package com.sidus.propert.service;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JWTService {
    String generateToken(String userName, String userRole, long expiration);
    String generateAccessToken(String userName, String userRole);
    String generateRefreshToken(String userName, String userRole);
    String getUsernameFromToken(String token);
    String getUserRoleFromToken(String token);
    Date getExpirationDateFromToken(String token);
    long getMaxAgeFromToken(String token);
    <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver);
    Boolean isTokenExpired(String token);
    Boolean isValid(String token);

}
