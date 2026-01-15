package io.code.sutra.activity.controller;

import io.code.sutra.activity.entity.Task;
import io.code.sutra.activity.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("/api/tasks/info")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String status) {
        log.info("Fetching tasks with status: {}", status);
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(taskService.getTasksByStatus(status));
        }
        List<Task> allTasks = taskService.getAllTasks();
        log.info("Retrieved {}", allTasks.toString());
        return ResponseEntity.ok(allTasks);
    }
    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/api/tasks/assignee/{name}")
    public ResponseEntity<List<Task>> findByAssignee(@PathVariable String name) {
        List<Task> allTasks = taskService.getTaskByAssignee(name.toLowerCase());
        log.info("Retrieved {}", allTasks.toString());
        return ResponseEntity.ok(allTasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.notFound().build();
    }
}