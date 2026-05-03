package org.example.fincore.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private String refreshSecret;
    private long accessExpMinutes;
    private long refreshExpDays;

    public long getAccessValidityMillis() {
        return Duration.ofMinutes(accessExpMinutes).toMillis();
    }
    public long getRefreshValidityMillis() {
        return Duration.ofDays(refreshExpDays).toMillis();
    }
}
