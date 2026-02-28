package com.bedoyarama.busnau.service;

import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User save(User user) {
    // Encode password if not already encoded
    if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // BCrypt prefix
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    return userRepository.save(user);
  }

  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  public void deleteById(Long id) {
    userRepository.deleteById(id);
  }
}
