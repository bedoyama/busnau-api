package com.bedoyarama.busnau.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

  private JwtUtils jwtUtils;

  @BeforeEach
  void setUp() {
    jwtUtils = new JwtUtils();
    ReflectionTestUtils.setField(
        jwtUtils,
        "jwtSecret",
        "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm");
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", 86400000L);
  }

  private Authentication authFor(String username) {
    UserDetails ud = new User(username, "password", Collections.emptyList());
    return new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
  }

  @Test
  void generateJwtToken_shouldReturnNonEmptyString() {
    String token = jwtUtils.generateJwtToken(authFor("alice"));
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void getUserNameFromJwtToken_shouldReturnOriginalUsername() {
    String token = jwtUtils.generateJwtToken(authFor("bob"));
    assertEquals("bob", jwtUtils.getUserNameFromJwtToken(token));
  }

  @Test
  void validateJwtToken_shouldReturnTrue_forValidToken() {
    String token = jwtUtils.generateJwtToken(authFor("carol"));
    assertTrue(jwtUtils.validateJwtToken(token));
  }

  @Test
  void validateJwtToken_shouldReturnFalse_forGarbageString() {
    assertFalse(jwtUtils.validateJwtToken("not.a.jwt"));
  }

  @Test
  void validateJwtToken_shouldReturnFalse_forExpiredToken() {
    // Set expiration to 0 ms → token expires immediately
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 0);
    String token = jwtUtils.generateJwtToken(authFor("dave"));
    assertFalse(jwtUtils.validateJwtToken(token));
  }

  @Test
  void generateRefreshToken_shouldDifferFromAccessToken() {
    Authentication auth = authFor("eve");
    String access = jwtUtils.generateJwtToken(auth);
    String refresh = jwtUtils.generateRefreshToken(auth);
    assertNotEquals(access, refresh, "access and refresh tokens must differ (different expiry)");
  }

  @Test
  void getJwtRefreshExpirationMs_shouldReturnConfiguredValue() {
    assertEquals(86400000L, jwtUtils.getJwtRefreshExpirationMs());
  }
}
