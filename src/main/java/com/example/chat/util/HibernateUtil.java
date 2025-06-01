package com.example.chat.util;

import com.example.chat.model.User; // Assuming User class is in this package
import com.example.chat.model.Message; // Uncommented as you likely have this entity

// Correct imports for Hibernate 6.x
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration; // This is the correct import for Configuration in H6 as well

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        try {
            // Using Configuration to load hibernate.cfg.xml is still a valid approach in Hibernate 6
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

            // Build the ServiceRegistry
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Create MetadataSources using the ServiceRegistry
            MetadataSources metadataSources = new MetadataSources(serviceRegistry);

            // Add your annotated entity classes to MetadataSources
            metadataSources.addAnnotatedClass(User.class);
            metadataSources.addAnnotatedClass(Message.class); // Ensure this is uncommented if it's an entity

            // Build Metadata from MetadataSources
            Metadata metadata = metadataSources.getMetadataBuilder().build();

            // Build SessionFactory from Metadata
            sessionFactory = metadata.getSessionFactoryBuilder().build();

        } catch (Exception e) {
            System.err.println("Initial SessionFactory creation failed: " + e);
            e.printStackTrace(); // Good practice to print the stack trace
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}