package com.retasify.productservice.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {
    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory();
    }

    @Bean
    public JtsModule jtsModule(GeometryFactory geometryFactory) {
        return new JtsModule(geometryFactory);
    }

    @Bean
    public ObjectMapper objectMapper(JtsModule jtsModule) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(jtsModule);
        return mapper;
    }
}
