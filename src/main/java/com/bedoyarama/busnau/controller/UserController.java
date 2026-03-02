package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.entity.Role;
import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
    logger.info("Creating user: {}", request.getUsername());

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(request.getPassword());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated =
        authentication != null
            && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getName());

    if (!isAuthenticated) {
      // Unauthenticated user creation (registration) - force USER role
      user.setRole(Role.USER);
      logger.info("Unauthenticated user creation, setting role to USER");
    } else {
      // Authenticated user creation
      String currentUsername = authentication.getName();
      User currentUser = userService.findByUsername(currentUsername);
      if (currentUser.getRole() == Role.ADMIN) {
        // ADMIN can create any role
        Role requestedRole =
            request.getRole() != null ? Role.valueOf(request.getRole().toUpperCase()) : Role.USER;
        user.setRole(requestedRole);
        logger.info("ADMIN user {} creating user with role {}", currentUsername, user.getRole());
      } else {
        // Non-ADMIN users can only create USER accounts
        user.setRole(Role.USER);
        logger.info("Non-ADMIN user {} creating user, forcing role to USER", currentUsername);
      }
    }

    User savedUser = userService.save(user);
    logger.info("User created with ID: {} and role: {}", savedUser.getId(), savedUser.getRole());
    return ResponseEntity.ok(savedUser);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    logger.info("Fetching user by ID: {}", id);
    if (isNotAdmin()) {
      logger.warn("Access denied: non-admin trying to fetch user by ID");
      return ResponseEntity.status(403).build();
    }
    Optional<User> user = userService.findById(id);
    if (user.isPresent()) {
      logger.info("User found: {}", user.get().getUsername());
      return ResponseEntity.ok(user.get());
    } else {
      logger.warn("User not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/username/{username}")
  public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
    logger.info("Fetching user by username: {}", username);
    if (isNotAdmin()) {
      logger.warn("Access denied: non-admin trying to fetch user by username");
      return ResponseEntity.status(403).build();
    }
    User user = userService.findByUsername(username);
    if (user != null) {
      logger.info("User found: {}", user.getUsername());
      return ResponseEntity.ok(user);
    } else {
      logger.warn("User not found with username: {}", username);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    logger.info("Fetching all users");
    if (isNotAdmin()) {
      logger.warn("Access denied: non-admin trying to fetch all users");
      return ResponseEntity.status(403).build();
    }
    List<User> users = userService.findAll();
    logger.info("Retrieved {} users", users.size());
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    logger.info("Deleting user with ID: {}", id);
    if (isNotAdmin()) {
      logger.warn("Access denied: non-admin trying to delete user");
      return ResponseEntity.status(403).build();
    }
    userService.deleteById(id);
    logger.info("User deleted with ID: {}", id);
    return ResponseEntity.noContent().build();
  }

  private boolean isNotAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getName())) {
      String currentUsername = authentication.getName();
      User currentUser = userService.findByUsername(currentUsername);
      return currentUser.getRole() != Role.ADMIN;
    }
    return true;
  }
}
