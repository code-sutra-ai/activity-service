package io.code.sutra.activity.service;

import io.code.sutra.activity.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskServiceBDDTest {

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private TaskService taskService;

    @Test
    @DisplayName("Given no tasks, when getAllTasks is called, then return empty list")
    void bddGetAllTasksReturnsEmptyList() {
        Mockito.when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(taskService.getAllTasks()).isEmpty();
    }
}
