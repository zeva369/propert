package com.sidus.propert.service.impl;

import com.sidus.propert.service.TokenStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenStoreServiceImpl implements TokenStoreService {

    private static final String TOKEN_PREFIX = "jwt_token:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public Boolean exists(String userId) {
        return redisTemplate.hasKey(TOKEN_PREFIX + userId);
    }

    @Override
    public String getToken(String userId) {
        return redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
    }

    @Override
    public void storeToken(String userId, String token) {
        redisTemplate.opsForValue().set(TOKEN_PREFIX + userId, token);
    }

    @Override
    public void delete(String userId) {
        redisTemplate.delete(TOKEN_PREFIX + userId);
    }
}
