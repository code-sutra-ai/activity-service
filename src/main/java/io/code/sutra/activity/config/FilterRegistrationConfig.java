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
        // Run last so this filter can override any Access-Control headers set by earlier filters
        reg.setOrder(Ordered.LOWEST_PRECEDENCE);
        reg.addUrlPatterns("/*");
        return reg;
    }
}
