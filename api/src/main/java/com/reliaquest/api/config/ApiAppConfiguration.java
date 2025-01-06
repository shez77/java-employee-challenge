package com.reliaquest.api.config;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ApiAppConfiguration {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(true);
        filter.setIncludeQueryString(true);
        return filter;
    }

    @Bean
    public RestClient restClient(
            @Value("${employee.server}") final String server, @Value("${employee.port}") final String port) {
        return RestClient.builder()
                .baseUrl(format("http://%s:%s", server, port))
                .build();
    }
}
