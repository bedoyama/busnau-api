package com.bedoyarama.busnau.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private TaskService taskService;

  @Test
  void findByUserIdAndDueDateBetween_shouldReturnTasksInRange() {
    // Given
    Long userId = 1L;
    LocalDate start = LocalDate.of(2026, 1, 1);
    LocalDate end = LocalDate.of(2026, 12, 31);
    List<Task> expectedTasks = List.of(new Task());

    when(taskRepository.findByUserIdAndDueDateBetween(userId, start, end)).thenReturn(expectedTasks);

    // When
    List<Task> tasks = taskService.findByUserIdAndDueDateBetween(userId, start, end);

    // Then
    verify(taskRepository).findByUserIdAndDueDateBetween(userId, start, end);
    assertEquals(expectedTasks, tasks);
  }
}
