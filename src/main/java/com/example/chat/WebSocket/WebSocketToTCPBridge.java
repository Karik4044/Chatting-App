package com.example.chat.WebSocket;

import com.example.chat.TCP.Client.TCPClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/tcp-bridge")
public class WebSocketToTCPBridge {
    private static final ConcurrentHashMap<Session, TCPClientForWeb> sessionToTCPClient = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToUsername = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("TCP Bridge WebSocket connection opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String type = jsonMessage.get("type").getAsString();

            switch (type) {
                case "login":
                    handleLogin(jsonMessage, session);
                    break;
                case "chat":
                    handleChatMessage(jsonMessage, session);
                    break;
                case "logout":
                    handleLogout(session);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket message: " + e.getMessage());
            sendErrorToClient(session, "Invalid message format");
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("TCP Bridge WebSocket connection closed: " + session.getId());
        cleanup(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("TCP Bridge WebSocket error for session " + session.getId() + ": " + throwable.getMessage());
        cleanup(session);
    }

    private void handleLogin(JsonObject loginMessage, Session session) {
        String username = loginMessage.get("username").getAsString();
        
        // Create TCP client for this session
        TCPClientForWeb tcpClient = new TCPClientForWeb(session);
        
        // Try to connect to TCP server
        if (tcpClient.connect()) {
            // Store the TCP client and username for this session
            sessionToTCPClient.put(session, tcpClient);
            sessionToUsername.put(session, username);
            
            // Send login to TCP server
            tcpClient.sendLogin(username, ""); // Password handled by web session
            
            // Send success response to web client
            JsonObject response = new JsonObject();
            response.addProperty("type", "login_success");
            response.addProperty("message", "Connected to TCP server successfully");
            sendToClient(session, gson.toJson(response));
            
            System.out.println("User " + username + " connected to TCP server via bridge");
        } else {
            // TCP server connection failed
            sendErrorToClient(session, "TCP Server is not available. Please make sure the server is running on port 8888.");
            System.err.println("Failed to connect user " + username + " to TCP server - server may not be running");
        }
    }

    private void handleChatMessage(JsonObject chatMessage, Session session) {
        TCPClientForWeb tcpClient = sessionToTCPClient.get(session);
        if (tcpClient != null && tcpClient.isConnected()) {
            Long recipientId = chatMessage.get("recipientId").getAsLong();
            String content = chatMessage.get("content").getAsString();
            
            tcpClient.sendChatMessage(recipientId, content);
        } else {
            sendErrorToClient(session, "Not connected to TCP server. Please refresh the page.");
        }
    }

    private void handleLogout(Session session) {
        cleanup(session);
    }

    private void cleanup(Session session) {
        TCPClientForWeb tcpClient = sessionToTCPClient.remove(session);
        if (tcpClient != null) {
            tcpClient.disconnect();
        }
        String username = sessionToUsername.remove(session);
        if (username != null) {
            System.out.println("User " + username + " disconnected from TCP bridge");
        }
    }

    private void sendToClient(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to WebSocket client: " + e.getMessage());
        }
    }

    private void sendErrorToClient(Session session, String error) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("type", "error");
        errorResponse.addProperty("message", error);
        sendToClient(session, gson.toJson(errorResponse));
    }
}
