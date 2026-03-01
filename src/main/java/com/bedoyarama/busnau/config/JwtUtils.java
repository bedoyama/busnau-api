package com.bedoyarama.busnau.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private int jwtExpirationMs;

  @Getter
  @Value("${jwt.refreshExpiration}")
  private long jwtRefreshExpirationMs;

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateJwtToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    assert userPrincipal != null;
    return Jwts.builder()
        .subject(userPrincipal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateRefreshToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    assert userPrincipal != null;
    return Jwts.builder()
        .subject(userPrincipal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(authToken);
      return true;
    } catch (MalformedJwtException e) {
      System.err.println("Invalid JWT token: " + e.getMessage());
    } catch (ExpiredJwtException e) {
      System.err.println("JWT token is expired: " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      System.err.println("JWT token is unsupported: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("JWT claims string is empty: " + e.getMessage());
    }
    return false;
  }
}
