package com.example.chat.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public SessionFactory sessionFactory() {
        // Sử dụng fully-qualified name để tránh xung đột
        return new org.hibernate.cfg.Configuration().configure().buildSessionFactory();
    }
}
