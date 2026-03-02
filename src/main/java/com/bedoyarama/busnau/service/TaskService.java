package com.bedoyarama.busnau.service;

import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public Task save(Task task) {
    return taskRepository.save(task);
  }

  public Optional<Task> findById(Long id) {
    return taskRepository.findById(id);
  }

  public List<Task> findAll() {
    return taskRepository.findAll();
  }

  public List<Task> findByUserId(Long userId) {
    return taskRepository.findByUser_Id(userId);
  }

  public List<Task> findByCompleted(Boolean completed) {
    return taskRepository.findByCompleted(completed);
  }

  public List<Task> findByUserIdAndDueDateBetween(Long userId, LocalDate start, LocalDate end) {
    return taskRepository.findByUser_IdAndDueDateBetween(userId, start, end);
  }

  public List<Task> findByUserIdAndCompleted(Long userId, Boolean completed) {
    return taskRepository.findByUser_IdAndCompleted(userId, completed);
  }

  public void deleteById(Long id) {
    taskRepository.deleteById(id);
  }
}
