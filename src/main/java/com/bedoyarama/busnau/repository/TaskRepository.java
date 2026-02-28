package com.bedoyarama.busnau.repository;

import com.bedoyarama.busnau.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByCompleted(Boolean completed);

}
