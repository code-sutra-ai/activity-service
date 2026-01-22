package io.code.sutra.activity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import io.code.sutra.activity.CognitoAuthenticationFilter;
import jakarta.servlet.FilterChain;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloWorld.class)
class HelloWorldControllerBDDTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CognitoAuthenticationFilter cognitoAuthenticationFilter;

    @Test
    @DisplayName("Given GET /hello, when called with Cognito JWT, then return 200 OK")
    void bddHelloWorldEndpoint() throws Exception {
        Mockito.doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(cognitoAuthenticationFilter).doFilter(Mockito.any(), Mockito.any(), Mockito.any());
        mockMvc.perform(get("/hello").header("Authorization", "Bearer dummy-jwt"))
                .andExpect(status().isOk());
    }
}
