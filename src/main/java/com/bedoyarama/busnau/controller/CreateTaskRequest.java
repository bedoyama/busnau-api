package com.bedoyarama.busnau.controller;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {
  @NotBlank(message = "Title is required")
  private String title;

  private String description;

  private LocalDate dueDate;

  private Boolean completed = false;

  private Long userId; // Optional, for ADMIN to assign to other users
}
