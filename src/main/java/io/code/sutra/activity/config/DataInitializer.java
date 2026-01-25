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
        // Initialize users with explicit ids and optional fields
        userRepository.save(new User(101L, "mukesh", "mukesh@jugaads.co.iz", "+1-555-0101", "Power user, admin"));
        userRepository.save(new User(102L, "elon", "elon@jugaads.co.iz", "+1-555-0202", "Investor"));
        userRepository.save(new User(103L, "jack", null, "+1-555-0303", ""));
        userRepository.save(new User(104L, "diana", "diana@jugaads.co.iz", null, "Music enthusiast"));

        // Initialize tasks with explicit ids
        taskRepository.save(new Task(11L, "Bike Repair", "completed", "mukesh", "bike-service"));
        taskRepository.save(new Task(12L, "Pay Tax", "pending", "mukesh", "bike-service"));
        taskRepository.save(new Task(13L, "Bill Pay", "in-progress", "elon", "billing-service"));
        taskRepository.save(new Task(14L, "Music Lessons", "pending", "diana", "music-service"));
    }
}