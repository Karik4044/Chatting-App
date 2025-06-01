package com.example.chat.config;

import com.example.chat.WebSocket.WebSocketToTCPBridge;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.DeploymentException;

@WebListener
public class WebSocketConfig implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
            .getAttribute("jakarta.websocket.server.ServerContainer");
        
        try {
            serverContainer.addEndpoint(WebSocketToTCPBridge.class);
            System.out.println("WebSocket endpoint registered successfully");
        } catch (DeploymentException e) {
            System.err.println("Failed to register WebSocket endpoint: " + e.getMessage());
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}