package com.cartesian.agentservice.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        mapper.findAndRegisterModules();
        return mapper;
    }
}
