package io.code.sutra.activity.service;
import ch.qos.logback.core.net.server.Client;
import io.code.sutra.activity.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import io.code.sutra.activity.entity.Task;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    public List<Task> getTaskByAssignee(String name) {
        return taskRepository.findByAssignee(name);
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

   public Task createTask(Task task) {

        Task savedTask = taskRepository.save(task);
        if (notificationService != null) {
            notificationService.notify("Task created successfully", "success");
        }
        return savedTask;
    }

    public Task updateTask(Long id, Task taskUpdates) {
        return taskRepository.findById(id)
            .map(existingTask -> {
                if (taskUpdates.getTitle() != null) {
                    existingTask.setTitle(taskUpdates.getTitle());
                }
                if (taskUpdates.getStatus() != null) {
                    existingTask.setStatus(taskUpdates.getStatus());
                }
                if (taskUpdates.getAssignee() != null) {
                    existingTask.setAssignee(taskUpdates.getAssignee());
                }
                Task updated = taskRepository.save(existingTask);
                if (notificationService != null) {
                    notificationService.notify("Task updated successfully", "success");
                }
                return updated;
            })
            .orElse(null);
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            if (notificationService != null) {
                notificationService.notify("Task deleted successfully", "success");
            }
            return true;
        }
        return false;
    }
}