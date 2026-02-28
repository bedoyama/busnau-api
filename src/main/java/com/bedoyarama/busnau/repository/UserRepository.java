package com.bedoyarama.busnau.repository;

import com.bedoyarama.busnau.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
