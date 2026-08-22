package com.cartesian.agentservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToFilePartConverter());
    }

    public static class StringToFilePartConverter implements Converter<String, FilePart> {
        @Override
        public FilePart convert(@NotNull String source) {
            if (source.trim().isEmpty() || "null".equalsIgnoreCase(source.trim())) {
                return null;
            }
            throw new IllegalArgumentException("Cannot convert non-empty String to FilePart");
        }
    }
}