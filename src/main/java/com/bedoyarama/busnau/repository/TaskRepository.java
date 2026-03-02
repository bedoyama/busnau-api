package com.bedoyarama.busnau.repository;

import com.bedoyarama.busnau.entity.Task;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByUser_Id(Long userId);

  List<Task> findByCompleted(Boolean completed);

  List<Task> findByUser_IdAndDueDateBetween(Long userId, LocalDate start, LocalDate end);

  List<Task> findByUser_IdAndCompleted(Long userId, Boolean completed);
}
