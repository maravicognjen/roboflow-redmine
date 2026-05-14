package com.example.roboflowredmine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor userAgentInterceptor = (request, body, execution) -> {
            request.getHeaders().set("User-Agent", "SpringBootApp/1.0");
            return execution.execute(request, body);
        };
        restTemplate.setInterceptors(Collections.singletonList(userAgentInterceptor));
        return restTemplate;
    }
}
