package com.seva.propert.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.seva.propert.converter.TaskInConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer{

     @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new TaskInConverter());
    }
}
