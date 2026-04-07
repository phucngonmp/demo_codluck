package org.example.demo.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.demo.entities.Account;
import org.example.demo.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-minutes}")
    private Long expirationMinutes;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.clock-skew-seconds}")
    private Long clockSkewSeconds;

    private Key signingKey;
    private JwtParser jwtParser;

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // Trim issuer to remove any whitespace from config
        String trimmedIssuer = issuer != null ? issuer.trim() : "sang-garden";
        this.jwtParser = Jwts.parserBuilder()
                .requireIssuer(trimmedIssuer)                 // enforce iss
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .setSigningKey(signingKey)
                .build();
        // Update issuer field to trimmed value for consistency
        this.issuer = trimmedIssuer;
    }


    private String createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        String trimmedIssuer = issuer != null ? issuer.trim() : "sang-garden";
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims) // Set custom claims first
                .setIssuer(trimmedIssuer) // Set issuer after claims to ensure it's not overwritten
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationMinutes*60)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims validateAndGetClaims(String token) throws JwtException {
        /*
            IF TOKEN IS INVALID THEN THE LINE BELOW WILL THROW EXCEPTIONS
         */
        Jws<Claims> jws = jwtParser.parseClaimsJws(token);
        Claims c = jws.getBody();

        // Defensive checks
        if (c.getSubject() == null || c.getExpiration() == null) {
            throw new JwtException("Missing sub/exp");
        }
        return c;
    }
}

