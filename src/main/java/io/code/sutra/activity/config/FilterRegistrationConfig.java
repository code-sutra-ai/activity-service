package io.code.sutra.activity.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterRegistrationConfig {

    @Bean
    public FilterRegistrationBean<EchoCorsFilter> echoCorsFilterRegistration(EchoCorsFilter filter) {
        FilterRegistrationBean<EchoCorsFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.addUrlPatterns("/*");
        return reg;
    }
}

