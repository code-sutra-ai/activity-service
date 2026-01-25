package io.code.sutra.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.code.sutra.activity.dto.UserRequest;
import io.code.sutra.activity.entity.User;
import io.code.sutra.activity.exception.ApiExceptionHandler;
import io.code.sutra.activity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerParameterizedTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob", "Sam"})
    void post_validNames_shouldReturnCreated(String name) throws Exception {
        User created = new User(501L, name);
        when(userService.createUser(eq(name))).thenReturn(created);

        UserRequest req = new UserRequest();
        req.setName(name);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value(created.getName()));
    }

    @ParameterizedTest
    @CsvSource({"'','name is required'", "'   ','name is required'", "NULL,'name is required'"})
    void post_invalidNames_shouldReturnBadRequest(String name, String expectedMessage) throws Exception {
        String payload;
        if ("NULL".equals(name)) {
            payload = "{}"; // missing name
        } else {
            // build JSON with the name value (may be empty/whitespace)
            payload = String.format("{\"name\":\"%s\"}", name);
        }

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.details.name").exists());
    }

    @ParameterizedTest
    @CsvSource({"1,alice,201,alice@example.com", "2,bob,202,bob@example.com"})
    void put_updateExistingUser_shouldReturnOk(Long id, String name, Long returnedId, String email) throws Exception {
        // Prepare request
        UserRequest req = new UserRequest();
        req.setName(name);
        req.setEmail(email);

        User updated = new User(returnedId, name, email, null, null);
        when(userService.updateUser(eq(id), any(User.class))).thenReturn(updated);

        String expectedName = name == null || name.isEmpty() ? name : (name.substring(0,1).toUpperCase() + name.substring(1));

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnedId))
                .andExpect(jsonPath("$.name").value(expectedName))
                .andExpect(jsonPath("$.email").value(email));
    }

    @ParameterizedTest
    @CsvSource({"10,nonexist", "11,ghost"})
    void put_updateNonExistingUser_shouldReturnNotFound(Long id, String name) throws Exception {
        UserRequest req = new UserRequest();
        req.setName(name);

        when(userService.updateUser(eq(id), any(User.class))).thenReturn(null);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
