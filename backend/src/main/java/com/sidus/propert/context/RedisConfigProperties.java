package com.sidus.propert.context;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.redis")
@Component
public class RedisConfigProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private int timeout;
    private boolean ssl;
    private int database;

}

