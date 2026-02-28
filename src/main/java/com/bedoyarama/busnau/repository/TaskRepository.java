package com.bedoyarama.busnau.repository;

import com.bedoyarama.busnau.entity.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByUserId(Long userId);

  List<Task> findByCompleted(Boolean completed);

  List<Task> findByUserIdAndDueDateBetween(Long userId, LocalDate start, LocalDate end);
}
