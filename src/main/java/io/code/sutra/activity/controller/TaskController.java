package io.code.sutra.activity.controller;

import io.code.sutra.activity.config.Constants;
import io.code.sutra.activity.dto.AssignRequest;
import io.code.sutra.activity.dto.TaskRequest;
import io.code.sutra.activity.entity.Task;
import io.code.sutra.activity.mapper.TaskMapper;
import io.code.sutra.activity.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.TASKS_BASE)
public class TaskController {

    private final TaskService taskService;

    @GetMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        log.info("Fetching task with id: {}", id);
        return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assignee/{name}")
    public ResponseEntity<List<Task>> findByAssignee(@PathVariable String name) {
        log.info("Fetching tasks for assignee: {}", name);
        List<Task> allTasks = taskService.getTaskByAssignee(name);
        log.info("Retrieved {}", allTasks.toString());
        return ResponseEntity.ok(allTasks);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest req) {
        Task task = TaskMapper.toEntity(req);
        Task created = taskService.createTask(task);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest taskReq) {
        log.info("Updating task with id: {} task: {}", id, taskReq);
        Task task = TaskMapper.toEntity(taskReq);
        Task updated = taskService.updateTask(id, task);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Task> assignTask(@PathVariable Long id, @Valid @RequestBody AssignRequest req) {
        log.info("Assigning task with id: {} to user: {}", id, req.getAssignee());
        Task updated = taskService.assignTask(id, req.getAssignee());
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}