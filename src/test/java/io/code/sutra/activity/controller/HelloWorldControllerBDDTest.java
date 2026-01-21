package io.code.sutra.activity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(HelloWorld.class)
class HelloWorldControllerBDDTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Given GET /hello, when called with valid JWT, then return 200 OK")
    void bddHelloWorldEndpoint() throws Exception {
        String jwtToken = System.getenv().getOrDefault("JWT_TOKEN", null);
        if (jwtToken == null) {
            throw new IllegalStateException("JWT_TOKEN environment variable must be set for endpoint testing.");
        }
        mockMvc.perform(get("/hello").header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
