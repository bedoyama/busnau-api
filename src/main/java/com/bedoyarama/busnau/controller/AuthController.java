package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.config.JwtUtils;
import com.bedoyarama.busnau.entity.RefreshToken;
import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.repository.RefreshTokenRepository;
import com.bedoyarama.busnau.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthenticationManager authenticationManager;

  private final JwtUtils jwtUtils;

  private final UserService userService;

  private final RefreshTokenRepository refreshTokenRepository;

  private final UserDetailsService userDetailsService;

  public AuthController(
      AuthenticationManager authenticationManager,
      JwtUtils jwtUtils,
      UserService userService,
      RefreshTokenRepository refreshTokenRepository,
      UserDetailsService userDetailsService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtils = jwtUtils;
    this.userService = userService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userDetailsService = userDetailsService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String accessToken = jwtUtils.generateJwtToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    User user = userService.findByUsername(loginRequest.getUsername());

    // Save refresh token
    RefreshToken refreshTokenEntity = new RefreshToken();
    refreshTokenEntity.setToken(refreshToken);
    refreshTokenEntity.setUser(user);
    refreshTokenEntity.setExpiryDate(
        Instant.now().plusMillis(jwtUtils.getJwtRefreshExpirationMs()));
    refreshTokenRepository.save(refreshTokenEntity);

    Map<String, Object> response = new HashMap<>();
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);
    response.put("id", user.getId());
    response.put("username", user.getUsername());
    response.put("role", user.getRole());

    logger.info("User {} logged in successfully", user.getUsername());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
    logger.info("Refresh token request received: {}", refreshRequest.getRefreshToken());
    String requestRefreshToken = refreshRequest.getRefreshToken();

    return refreshTokenRepository
        .findByToken(requestRefreshToken)
        .map(
            refreshToken -> {
              if (refreshToken.isExpired()) {
                refreshTokenRepository.delete(refreshToken);
                return ResponseEntity.badRequest().body("Refresh token is expired");
              }

              User user = refreshToken.getUser();
              UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
              UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

              String newAccessToken = jwtUtils.generateJwtToken(auth);

              Map<String, Object> response = new HashMap<>();
              response.put("accessToken", newAccessToken);
              response.put("refreshToken", requestRefreshToken);

              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.badRequest().body("Refresh token not found"));
  }

  @Setter
  @Getter
  public static class LoginRequest {
    // Getters and setters
    private String username;
    private String password;
  }

  @Setter
  @Getter
  public static class RefreshRequest {
    private String refreshToken;
  }
}
