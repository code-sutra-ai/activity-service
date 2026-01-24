package io.code.sutra.activity.config;

import io.code.sutra.activity.entity.Task;
import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.repository.TaskRepository;
import io.code.sutra.activity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Initialize users
        userRepository.save(new User(101L, "mukesh"));
        userRepository.save(new User(102L, "elon"));
        userRepository.save(new User(103L, "jack"));
        userRepository.save(new User(104L, "diana"));

        // Initialize tasks
        taskRepository.save(new Task(11L, "Bike Repair", "completed", "mukesh", "bike-service"));
        taskRepository.save(new Task(12L, "Pay Tax", "pending", "mukesh", "bike-service"));
        taskRepository.save(new Task(13L, "Bill Pay", "in-progress", "elon", "billing-service"));
        taskRepository.save(new Task(14L, "Music Lessons", "pending", "diana", "music-service"));
    }
}