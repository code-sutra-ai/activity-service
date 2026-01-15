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
        userRepository.save(new User(null, "mukesh"));
        userRepository.save(new User(null, "elon"));
        userRepository.save(new User(null, "jack"));
        userRepository.save(new User(null, "diana"));

        // Initialize tasks
        taskRepository.save(new Task(null, "Bike Repair", "completed", "mukesh", "bike-service"));
        taskRepository.save(new Task(null, "Pay Tax", "pending", "mukesh", "bike-service"));
        taskRepository.save(new Task(null, "Bill Pay", "in-progress", "elon", "billing-service"));
        taskRepository.save(new Task(null, "Music Lessons", "pending", "diana", "music-service"));
    }
}