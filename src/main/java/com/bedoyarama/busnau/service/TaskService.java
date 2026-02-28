package com.bedoyarama.busnau.service;

import com.bedoyarama.busnau.entity.Task;
import com.bedoyarama.busnau.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return taskRepository.findByUserId(userId);
    }

    public List<Task> findByCompleted(Boolean completed) {
        return taskRepository.findByCompleted(completed);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

}
