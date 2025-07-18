package com.sidus.propert.context;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final RedisConfigProperties redisConfigProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisConfigProperties.getHost());
        config.setPort(redisConfigProperties.getPort());
        config.setDatabase(redisConfigProperties.getDatabase());
        config.setUsername(redisConfigProperties.getUsername()); // Usuario configurado en Redis
        config.setPassword(redisConfigProperties.getPassword()); // Contraseña configurada en Redis
        return new JedisConnectionFactory(config);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .build();
    }

}