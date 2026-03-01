package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.config.JwtUtils;
import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthenticationManager authenticationManager;

  private final JwtUtils jwtUtils;

  private final UserService userService;

  public AuthController(
      AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtils = jwtUtils;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    User user = userService.findByUsername(loginRequest.getUsername());
    Map<String, Object> response = new HashMap<>();
    response.put("token", jwt);
    response.put("id", user.getId());
    response.put("username", user.getUsername());
    response.put("role", user.getRole());

    logger.info("User {} logged in successfully", user.getUsername());
    return ResponseEntity.ok(response);
  }

  @Setter
  @Getter
  public static class LoginRequest {
    // Getters and setters
    private String username;
    private String password;
  }
}
