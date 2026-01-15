package io.code.sutra.activity.service;

import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.repository.UserRepository;
import io.code.sutra.activity.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private NotificationService notificationService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
   public User createUser(String name) {
        User user = new User(IdGenerator.getNextId(),name);
        User savedUser = userRepository.save(user);
        notificationService.notify("User created successfully", "success");
        return savedUser;
    }

    public User updateUser(Long id, User userUpdates) {
        return userRepository.findById(id)
            .map(existingUser -> {
                if (userUpdates.getName() != null) {
                    existingUser.setName(userUpdates.getName());
                }
                User updated = userRepository.save(existingUser);
                notificationService.notify("User updated successfully", "success");
                return updated;
            })
            .orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            notificationService.notify("User deleted successfully", "success");
            return true;
        }
        return false;
    }
}