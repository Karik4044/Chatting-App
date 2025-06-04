// src/main/java/com/example/chat/config/WebConfig.java
package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableWebMvc
public class WebConfig {
  @Bean
  public InternalResourceViewResolver jspViewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/jsp/");
    resolver.setSuffix(".jsp");
    return resolver;
  }
}