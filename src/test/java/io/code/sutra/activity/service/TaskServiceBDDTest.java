package io.code.sutra.activity.service;

import io.code.sutra.activity.repository.TaskRepository;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SerenityJUnit5Extension.class, MockitoExtension.class})
class TaskServiceBDDTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Given no tasks, when getAllTasks is called, then return empty list")
    void bddGetAllTasksReturnsEmptyList() {
        Mockito.when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(taskService.getAllTasks()).isEmpty();
    }
}
