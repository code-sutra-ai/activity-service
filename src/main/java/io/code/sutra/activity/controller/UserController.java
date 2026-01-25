package io.code.sutra.activity.controller;

import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> allUsers = userService.getAllUsers();
        log.info("Retrieved {}", allUsers.toString());
        return ResponseEntity.ok(allUsers);
    }

    @PostMapping("/api/users")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        try {
            log.info("Received request to add user: {}", user);
            if (user == null || user.getName() == null || user.getName().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "name is required"));
            }
            User created = userService.createUser(user.getName().trim());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", created.getName(), "id", created.getId()));
        } catch (RuntimeException e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            log.info("Received request to update user id={} with data: {}", id, user);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "request body is required"));
            }
            // Ensure name is present if provided and not blank
            if (user.getName() != null && user.getName().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "name cannot be blank"));
            }
            User updated = userService.updateUser(id, user);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(Map.of("id", updated.getId(), "name", updated.getName(), "email", updated.getEmail(), "phone", updated.getPhone(), "notes", updated.getNotes()));
        } catch (RuntimeException e) {
            log.error("Error updating user id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user id={}", id);
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        } catch (RuntimeException e) {
            log.error("Error deleting user id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}