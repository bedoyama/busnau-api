package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.service.TaskService;
import com.bedoyarama.busnau.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
  private final TaskService taskService;
  private final UserService userService;

  public TaskController(TaskService taskService, UserService userService) {
    this.taskService = taskService;
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<Task> createTask(@RequestBody @Valid Task task) {
    logger.info("Creating task: {}", task.getTitle());
    // Assuming task has userId in JSON, fetch User
    if (task.getUser() == null && task.getUserId() != null) {
      User user = userService.findById(task.getUserId()).orElse(null);
      if (user == null) {
        logger.warn("User not found for ID: {}", task.getUserId());
        return ResponseEntity.badRequest().build();
      }
      task.setUser(user);
    }
    Task savedTask = taskService.save(task);
    logger.info("Task created with ID: {}", savedTask.getId());
    return ResponseEntity.ok(savedTask);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
    logger.info("Fetching task by ID: {}", id);
    Optional<Task> task = taskService.findById(id);
    if (task.isPresent()) {
      logger.info("Task found: {}", task.get().getTitle());
      return ResponseEntity.ok(task.get());
    } else {
      logger.warn("Task not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<Task>> getAllTasks() {
    logger.info("Fetching all tasks");
    List<Task> tasks = taskService.findAll();
    logger.info("Retrieved {} tasks", tasks.size());
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
    logger.info("Fetching tasks for user ID: {}", userId);
    List<Task> tasks = taskService.findByUserId(userId);
    logger.info("Retrieved {} tasks for user {}", tasks.size(), userId);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/completed/{completed}")
  public ResponseEntity<List<Task>> getTasksByCompleted(@PathVariable Boolean completed) {
    logger.info("Fetching tasks with completed status: {}", completed);
    List<Task> tasks = taskService.findByCompleted(completed);
    logger.info("Retrieved {} tasks with completed: {}", tasks.size(), completed);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}/date-range")
  public ResponseEntity<List<Task>> getTasksByUserIdAndDateRange(
      @PathVariable Long userId,
      @RequestParam LocalDate start,
      @RequestParam LocalDate end) {
    logger.info("Fetching tasks for user {} between {} and {}", userId, start, end);
    List<Task> tasks = taskService.findByUserIdAndDueDateBetween(userId, start, end);
    logger.info("Retrieved {} tasks for user {} in date range", tasks.size(), userId);
    return ResponseEntity.ok(tasks);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    logger.info("Deleting task with ID: {}", id);
    taskService.deleteById(id);
    logger.info("Task deleted with ID: {}", id);
    return ResponseEntity.noContent().build();
  }
}
