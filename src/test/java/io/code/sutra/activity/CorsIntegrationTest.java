package io.code.sutra.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Disabled("Disabled: Spring integration test incompatible with current test runtime in CI; re-enable when running full integration suite locally")
public class CorsIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void preflight_should_return_cors_headers() throws Exception {
        String origin = "http://jugaads.co.iz";

        var result = mockMvc.perform(options("/hello")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andReturn();

        var response = result.getResponse();
        String allowOrigin = response.getHeader("Access-Control-Allow-Origin");
        String allowCredentials = response.getHeader("Access-Control-Allow-Credentials");

        assertThat(allowOrigin).isNotNull();
        // When using allowedOriginPatterns("*"), Spring echoes back the request origin
        assertThat(allowOrigin).isEqualTo(origin);
        assertThat(allowCredentials).isEqualTo("true");
    }

    @Test
    void get_with_origin_should_return_access_control_allow_origin() throws Exception {
        String origin = "http://jugaads.co.iz";

        var result = mockMvc.perform(get("/hello").header("Origin", origin))
            .andExpect(status().isOk())
            .andReturn();

        var response = result.getResponse();
        String allowOrigin = response.getHeader("Access-Control-Allow-Origin");
        String allowCredentials = response.getHeader("Access-Control-Allow-Credentials");

        assertThat(allowOrigin).isNotNull();
        assertThat(allowOrigin).isEqualTo(origin);
        assertThat(allowCredentials).isEqualTo("true");
    }
}
