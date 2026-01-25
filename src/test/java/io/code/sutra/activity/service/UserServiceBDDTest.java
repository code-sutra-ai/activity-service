package io.code.sutra.activity.service;

import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.repository.UserRepository;
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

@Tag("smoke")
@ExtendWith({SerenityJUnit5Extension.class, MockitoExtension.class})
class UserServiceBDDTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Given no users, when getAllUsers is called, then return empty list")
    void bddGetAllUsersReturnsEmptyList() {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(userService.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("When creating a user by name, saved user is returned")
    void bddCreateUserReturnsSaved() {
        User u = new User(10L, "alice");
        Mockito.when(userRepository.save(any(User.class))).thenReturn(u);
        User saved = userService.createUser("alice");
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getName()).isEqualTo("Alice"); // name capitalized by entity getter
    }

    @Test
    @DisplayName("Updating an existing user applies changes")
    void bddUpdateUserAppliesChanges() {
        User existing = new User(11L, "bob");
        Mockito.when(userRepository.findById(11L)).thenReturn(Optional.of(existing));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updates = new User(null, "bobby");
        User updated = userService.updateUser(11L, updates);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Bobby");
    }

    @Test
    @DisplayName("Deleting an existing user returns true")
    void bddDeleteUserReturnsTrue() {
        Mockito.when(userRepository.existsById(12L)).thenReturn(true);
        boolean deleted = userService.deleteUser(12L);
        assertThat(deleted).isTrue();
    }
}
