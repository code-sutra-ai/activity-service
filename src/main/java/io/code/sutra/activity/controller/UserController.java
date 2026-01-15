package io.code.sutra.activity.controller;

import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/api/users/info")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @RequestMapping("/api/users/add" )
    public ResponseEntity<?> addUser(@RequestBody Map<String, String> request) {
        try {
            System.out.println("Received request to add user: " + request);
            String name = request.get("name");
            User user = userService.createUser(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(user.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /*@PutMapping
    public ResponseEntity<?> updateUser(@RequestBody Map<String, String> request) {
        try {
            String oldName = request.get("oldName");
            String newName = request.get("newName");
            User user = userService.updateUser(oldName, newName);
            return ResponseEntity.ok(user.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
*/
    @DeleteMapping("/api/users/{name}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}