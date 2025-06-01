package com.example.chat.WebSocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.chat.TCP.Client.TCPClient;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, TCPClient> tcpClients = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connection opened: " + session.getId());
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
                default:
                    sendError(session, "Unknown message type: " + type);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace(); // For better debugging
            sendError(session, "Invalid message format or server error");
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        handleLogout(session);
        System.out.println("WebSocket connection closed: " + session.getId() + ", Reason: " + closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error for session " + (session != null ? session.getId() : "unknown") + ": " + throwable.getMessage());
        throwable.printStackTrace(); // For better debugging
        if (session != null && session.isOpen()) {
            sendError(session, "An unexpected error occurred.");
        }
        // Optionally, ensure resources are cleaned up if the error is critical
        // handleLogout(session); // Consider if appropriate here
    }

    private void handleLogin(JsonObject jsonMessage, Session session) {
        String username = jsonMessage.get("username").getAsString();

        // Tạo TCP client connection
        TCPClient tcpClient = new TCPClient();
        if (tcpClient.connect()) {
            // Assuming sendLogin might involve session token handling not shown
            tcpClient.sendLogin(username, "session_token_placeholder"); // Use a real session token or mechanism
            tcpClients.put(session, tcpClient);
            sessions.put(username, session);

            // Gửi phản hồi thành công
            JsonObject response = new JsonObject();
            response.addProperty("type", "login_success");
            response.addProperty("username", username);
            sendMessage(session, gson.toJson(response));
            System.out.println("User logged in: " + username + " with session " + session.getId());
        } else {
            sendError(session, "Failed to connect to TCP server");
        }
    }

    private void handleChatMessage(JsonObject jsonMessage, Session session) {
        TCPClient tcpClient = tcpClients.get(session);
        if (tcpClient != null) {
            Long recipientId = jsonMessage.get("recipientId").getAsLong();
            String content = jsonMessage.get("content").getAsString();
            tcpClient.sendChatMessage(recipientId, content);
            // Optionally send an ack back to the sender
            // JsonObject ack = new JsonObject();
            // ack.addProperty("type", "message_sent");
            // sendMessage(session, gson.toJson(ack));
        } else {
            sendError(session, "Not logged in or TCP client not available.");
        }
    }

    private void handleLogout(Session session) {
        TCPClient tcpClient = tcpClients.remove(session);
        if (tcpClient != null) {
            tcpClient.sendLogout();
            tcpClient.disconnect();
        }

        // Correctly remove the session from the sessions map
        // Find the username associated with the session and remove it
        String userToRemove = null;
        for (ConcurrentHashMap.Entry<String, Session> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                userToRemove = entry.getKey();
                break;
            }
        }
        if (userToRemove != null) {
            sessions.remove(userToRemove);
            System.out.println("User logged out: " + userToRemove);
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("Error sending message to session " + (session != null ? session.getId() : "unknown") + ": " + e.getMessage());
            // Consider removing session if sending fails persistently
        }
    }

    private void sendError(Session session, String error) {
        JsonObject errorMsg = new JsonObject();
        errorMsg.addProperty("type", "error");
        errorMsg.addProperty("message", error);
        sendMessage(session, gson.toJson(errorMsg));
    }

    public static void broadcastMessage(String message) {
        for (Session session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting message to session " + session.getId() + ": " + e.getMessage());
            }
        }
    }
}