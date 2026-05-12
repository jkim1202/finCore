package org.example.fincore.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init(){
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()));
    }

    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getAccessValidityMillis());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(accessKey)
                .compact();
    }
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshValidityMillis());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(refreshKey)
                .compact();
    }
    public String getEmailFromAccessToken(String token){
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        validateToken(token,accessKey);
        String tokenEmail = getEmailFromAccessToken(token);
        return tokenEmail != null && tokenEmail.equals(userDetails.getUsername());
    }
    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        validateToken(token,refreshKey);
        String tokenEmail = getEmailFromRefreshToken(token);
        return tokenEmail != null && tokenEmail.equals(userDetails.getUsername());
    }
    public String getEmailFromRefreshToken(String token) {
        validateToken(token,refreshKey);
        return getClaimFromToken(token,refreshKey,Claims::getSubject);
    }

    private void validateToken(String token, SecretKey secretKey){
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch(ExpiredJwtException e){
            throw new BusinessException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch(UnsupportedJwtException e){
            throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_TOKEN);
        } catch(MalformedJwtException | SignatureException | IllegalArgumentException e){
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }
    private <T> T getClaimFromToken(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token, key);
        return claimsResolver.apply(claims);
    }
    public Claims getAllClaimsFromToken(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
