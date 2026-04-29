package org.example.fincore.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private String refreshSecret;
    private long accessExpMinutes;
    private long refreshExpDays;
}
