package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.service.UserService;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    logger.info("Creating user: {}", user.getUsername());
    User savedUser = userService.save(user);
    logger.info("User created with ID: {}", savedUser.getId());
    return ResponseEntity.ok(savedUser);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    logger.info("Fetching user by ID: {}", id);
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
    List<User> users = userService.findAll();
    logger.info("Retrieved {} users", users.size());
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    logger.info("Deleting user with ID: {}", id);
    userService.deleteById(id);
    logger.info("User deleted with ID: {}", id);
    return ResponseEntity.noContent().build();
  }
}
