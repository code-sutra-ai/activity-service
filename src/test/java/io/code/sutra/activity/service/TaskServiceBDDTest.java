package io.code.sutra.activity.service;

import io.code.sutra.activity.entity.Task;
import io.code.sutra.activity.repository.TaskRepository;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Tag("smoke")
@ExtendWith({SerenityJUnit5Extension.class, MockitoExtension.class})
class TaskServiceBDDTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Given no tasks, when getAllTasks is called, then return empty list")
    void bddGetAllTasksReturnsEmptyList() {
        Mockito.when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    @DisplayName("When creating a task, saved task is returned")
    void bddCreateTaskReturnsSaved() {
        Task t = new Task(1L, "T1", "pending", null, "svc");
        Mockito.when(taskRepository.save(any(Task.class))).thenReturn(t);
        Task saved = taskService.createTask(new Task(null, "T1", "pending", null, "svc"));
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Assigning a task updates assignee")
    void bddAssignTaskUpdatesAssignee() {
        Task existing = new Task(2L, "T2", "pending", null, "svc");
        Mockito.when(taskRepository.findById(2L)).thenReturn(Optional.of(existing));
        Mockito.when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskService.assignTask(2L, "alice");
        assertThat(updated).isNotNull();
        // Task.getAssignee capitalizes first letter, so expect "Alice"
        assertThat(updated.getAssignee()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Deleting existing task returns true")
    void bddDeleteTaskReturnsTrueIfExists() {
        Mockito.when(taskRepository.existsById(3L)).thenReturn(true);
        boolean deleted = taskService.deleteTask(3L);
        assertThat(deleted).isTrue();
    }
}
