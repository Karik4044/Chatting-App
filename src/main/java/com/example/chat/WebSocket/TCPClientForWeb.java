package com.example.chat.WebSocket;

import com.example.chat.TCP.Client.TCPClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.Session;
import java.io.IOException;

public class TCPClientForWeb extends TCPClient {
    private final Session webSocketSession;
    private final Gson gson;

    public TCPClientForWeb(Session webSocketSession) {
        super();
        this.webSocketSession = webSocketSession;
        this.gson = new Gson();
    }

    @Override
    protected void handleServerMessage(String message) {
        try {
            // Parse the TCP server message
            JsonObject tcpMessage = gson.fromJson(message, JsonObject.class);
            
            // Forward the message to the web client through WebSocket
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            System.err.println("Error forwarding TCP message to WebSocket: " + e.getMessage());
        }
    }

    public void sendMessageToWebClient(String message) {
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to web client: " + e.getMessage());
        }
    }
}