package com.bedoyarama.busnau.repository;

import com.bedoyarama.busnau.entity.RefreshToken;
import com.bedoyarama.busnau.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  @Modifying
  int deleteByUser(User user);
}
