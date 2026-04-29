package org.example.fincore.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
}
