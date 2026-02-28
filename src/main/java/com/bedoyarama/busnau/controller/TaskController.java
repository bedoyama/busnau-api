package com.bedoyarama.busnau.controller;

import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.service.TaskService;
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

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  public ResponseEntity<Task> createTask(@RequestBody Task task) {
    logger.info("Creating task: {}", task.getTitle());
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

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    logger.info("Deleting task with ID: {}", id);
    taskService.deleteById(id);
    logger.info("Task deleted with ID: {}", id);
    return ResponseEntity.noContent().build();
  }
}
