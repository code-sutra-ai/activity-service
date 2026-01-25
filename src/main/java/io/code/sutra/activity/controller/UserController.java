package io.code.sutra.activity.controller;

import io.code.sutra.activity.config.Constants;
import io.code.sutra.activity.dto.UserRequest;
import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.mapper.UserMapper;
import io.code.sutra.activity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.USERS_BASE)
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> addUser(@Valid @RequestBody UserRequest userReq) {
        try {
            log.info("Received request to add user: {}", userReq);
            User created = userService.createUser(userReq.getName().trim());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", created.getName(), "id", created.getId()));
        } catch (RuntimeException e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userReq) {
        try {
            User updated = userService.updateUser(id, UserMapper.toEntity(userReq));
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(Map.of("id", updated.getId(), "name", updated.getName(), "email", updated.getEmail(), "phone", updated.getPhone(), "notes", updated.getNotes()));
        } catch (RuntimeException e) {
            log.error("Error updating user id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
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