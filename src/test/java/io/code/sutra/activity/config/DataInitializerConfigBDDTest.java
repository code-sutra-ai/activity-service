package io.code.sutra.activity.config;

import io.code.sutra.activity.config.DataInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Disabled: Integration test requires Spring context and DB; re-enable for local integration testing")
class DataInitializerConfigBDDTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Given context loads, when DataInitializer bean is present, then it should not be null")
    void bddDataInitializerBeanPresent() {
        assertThat(dataInitializer).isNotNull();
    }
}
