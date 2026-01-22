package io.code.sutra.activity.controller;

import io.code.sutra.activity.controller.HelloWorld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldControllerBDDTest {

    @Test
    @DisplayName("Given direct call to hello(), then return 200 OK")
    void bddHelloWorldEndpoint() {
        HelloWorld controller = new HelloWorld();
        var response = controller.hello();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Hello, World!");
    }
}
