package com.bedoyarama.busnau.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  @Test
  void save_shouldEncodePasswordIfNotAlreadyEncoded() {
    // Given
    User user = new User();
    user.setUsername("testuser");
    user.setPassword("plainpassword");
    user.setRole("USER");

    when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    User savedUser = userService.save(user);

    // Then
    verify(passwordEncoder).encode("plainpassword");
    verify(userRepository).save(user);
    assertEquals("encodedpassword", savedUser.getPassword());
  }

  @Test
  void save_shouldNotEncodePasswordIfAlreadyEncoded() {
    // Given
    User user = new User();
    user.setUsername("testuser");
    user.setPassword("$2a$10$encodedpassword");
    user.setRole("USER");

    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    User savedUser = userService.save(user);

    // Then
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository).save(user);
  }
}
