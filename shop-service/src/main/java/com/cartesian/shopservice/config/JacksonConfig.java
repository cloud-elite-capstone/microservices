package com.cartesian.shopservice.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }

    @Bean
    public ObjectMapper objectMapper(JtsModule jtsModule) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jtsModule);
        return mapper;
    }
}
