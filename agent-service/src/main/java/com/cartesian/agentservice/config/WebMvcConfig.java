package com.cartesian.agentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.validation.constraints.NotNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToMultipartFileConverter());
    }

    public static class StringToMultipartFileConverter implements Converter<String, MultipartFile> {
        @Override
        public MultipartFile convert(@NotNull String source) {
            if (source.trim().isEmpty() || "null".equalsIgnoreCase(source.trim())) {
                return null;
            }
            throw new IllegalArgumentException("Cannot convert non-empty String to MultipartFile");
        }
    }
}
