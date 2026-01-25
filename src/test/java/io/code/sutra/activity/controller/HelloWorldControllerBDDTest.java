package io.code.sutra.activity.controller;

import io.code.sutra.activity.controller.HelloWorld;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("smoke")
@ExtendWith(SerenityJUnit5Extension.class)
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
