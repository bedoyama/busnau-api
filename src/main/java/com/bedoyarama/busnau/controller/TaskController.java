package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.entity.Role;
import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.entity.User;
import com.bedoyarama.busnau.service.TaskService;
import com.bedoyarama.busnau.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
  public ResponseEntity<Task> createTask(@RequestBody @Valid CreateTaskRequest request) {
    logger.info("Creating task: {}", request.getTitle());

    User currentUser = getCurrentUser();
    User taskUser = currentUser;

    // Allow ADMIN to assign task to another user
    if (request.getUserId() != null && currentUser.getRole() == Role.ADMIN) {
      Optional<User> targetUser = userService.findById(request.getUserId());
      if (targetUser.isPresent()) {
        taskUser = targetUser.get();
        logger.info(
            "ADMIN {} creating task for user {}",
            currentUser.getUsername(),
            taskUser.getUsername());
      } else {
        logger.warn(
            "ADMIN {} tried to create task for non-existent user {}",
            currentUser.getUsername(),
            request.getUserId());
        return ResponseEntity.badRequest().body(null);
      }
    }

    Task task = new Task();
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setDueDate(request.getDueDate());
    task.setCompleted(request.getCompleted() != null ? request.getCompleted() : false);
    task.setUser(taskUser);

    Task savedTask = taskService.save(task);
    logger.info("Task created with ID: {} for user {}", savedTask.getId(), taskUser.getUsername());
    return ResponseEntity.ok(savedTask);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
    logger.info("Fetching task by ID: {}", id);
    Optional<Task> task = taskService.findById(id);
    if (task.isPresent()) {
      User currentUser = getCurrentUser();
      if (currentUser.getRole() != Role.ADMIN
          && !task.get().getUser().getId().equals(currentUser.getId())) {
        logger.warn(
            "Access denied: task {} does not belong to user {}", id, currentUser.getUsername());
        return ResponseEntity.status(403).build();
      }
      logger.info("Task found: {}", task.get().getTitle());
      return ResponseEntity.ok(task.get());
    } else {
      logger.warn("Task not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<Task>> getAllTasks() {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() == Role.ADMIN) {
      logger.info("Fetching all tasks (ADMIN access)");
      List<Task> tasks = taskService.findAll();
      logger.info("Retrieved {} tasks", tasks.size());
      return ResponseEntity.ok(tasks);
    } else {
      logger.info("Fetching all tasks for current user");
      List<Task> tasks = taskService.findByUserId(currentUser.getId());
      logger.info("Retrieved {} tasks for user {}", tasks.size(), currentUser.getUsername());
      return ResponseEntity.ok(tasks);
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
    User currentUser = getCurrentUser();
    logger.info(
        "Current user: {} id: {}, role: {}, requested userId: {}",
        currentUser.getUsername(),
        currentUser.getId(),
        currentUser.getRole(),
        userId);
    if (currentUser.getRole() != Role.ADMIN && !userId.equals(currentUser.getId())) {
      logger.warn(
          "Access denied: user {} trying to access tasks of user {}",
          currentUser.getUsername(),
          userId);
      return ResponseEntity.status(403).build();
    }
    logger.info("Fetching tasks for user ID: {}", userId);
    List<Task> tasks = taskService.findByUserId(userId);
    logger.info("Retrieved {} tasks for user {}", tasks.size(), userId);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/completed/{completed}")
  public ResponseEntity<List<Task>> getTasksByCompleted(@PathVariable Boolean completed) {
    logger.info("Fetching tasks with completed status: {} for current user", completed);
    User currentUser = getCurrentUser();
    List<Task> tasks = taskService.findByUserIdAndCompleted(currentUser.getId(), completed);
    logger.info(
        "Retrieved {} tasks with completed: {} for user {}",
        tasks.size(),
        completed,
        currentUser.getUsername());
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}/date-range")
  public ResponseEntity<List<Task>> getTasksByUserIdAndDateRange(
      @PathVariable Long userId, @RequestParam LocalDate start, @RequestParam LocalDate end) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.ADMIN && !userId.equals(currentUser.getId())) {
      logger.warn(
          "Access denied: user {} trying to access date-range tasks of user {}",
          currentUser.getUsername(),
          userId);
      return ResponseEntity.status(403).build();
    }
    logger.info("Fetching tasks for user {} between {} and {}", userId, start, end);
    List<Task> tasks = taskService.findByUserIdAndDueDateBetween(userId, start, end);
    logger.info("Retrieved {} tasks for user {} in date range", tasks.size(), userId);
    return ResponseEntity.ok(tasks);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    logger.info("Deleting task with ID: {}", id);
    Optional<Task> task = taskService.findById(id);
    if (task.isPresent()) {
      User currentUser = getCurrentUser();
      if (currentUser.getRole() != Role.ADMIN
          && !task.get().getUser().getId().equals(currentUser.getId())) {
        logger.warn(
            "Access denied: user {} trying to delete task {} of another user",
            currentUser.getUsername(),
            id);
        return ResponseEntity.status(403).build();
      }
      taskService.deleteById(id);
      logger.info("Task deleted with ID: {}", id);
      return ResponseEntity.noContent().build();
    } else {
      logger.warn("Task not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  private User getCurrentUser() {
    String username =
        Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    return userService.findByUsername(username);
  }
}
