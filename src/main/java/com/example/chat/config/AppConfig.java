package com.example.chat.config;

import com.example.chat.TCP.Server.TCPServer;
import com.example.chat.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@ComponentScan("com.example.chat")
@EnableWebSocket
public class AppConfig{

    @Bean
    public TCPServer tcpServer() {
        TCPServer tcpServer = new TCPServer();
        new Thread(tcpServer).start();
        return tcpServer;
    }


//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(chatWebSocketHandler(tcpServer()), "/chat").setAllowedOrigins("*");
//    }
}