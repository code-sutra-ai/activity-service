package io.code.sutra.activity.config;

import io.code.sutra.activity.config.DataInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(DataInitializer.class)
class DataInitializerConfigBDDTest {

    @MockitoBean
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Given context loads, when DataInitializer bean is present, then it should not be null")
    void bddDataInitializerBeanPresent() {
        assertThat(dataInitializer).isNotNull();
    }
}
