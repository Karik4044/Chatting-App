package com.example.chat.TCP.Server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.chat.DAO.messagedao;
import com.example.chat.DAO.UserDAO;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.util.SecurityUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Long userId;
    private User user;
    private UserDAO userDAO;
    private messagedao messageDAO;
    private Gson gson;

    public ClientHandler(Socket socket, TCPServer server) {
        this.clientSocket = socket;
        this.userDAO = new UserDAO();
        this.messageDAO = new messagedao();
        this.gson = new Gson();
    }
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                handleMessage(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleMessage(String message) {
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String type = jsonMessage.get("type").getAsString();
            
            switch (type) {
                case "login":
                    handleLogin(jsonMessage);
                    break;
                case "chat":
                    handleChatMessage(jsonMessage);
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "get_users":
                    handleGetUsers();
                    break;
                default:
                    sendErrorMessage("Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendErrorMessage("Invalid message format");
        }
    }
    
    private void handleLogin(JsonObject jsonMessage) {
        String username = jsonMessage.get("username").getAsString();
        String password = jsonMessage.get("password").getAsString();
        
        User user = userDAO.getUserByUsername(username);
        if (user != null && SecurityUtil.verifyPassword(password, user.getPassword())) {
            this.user = user;
            this.userId = user.getId();
            
            // Update user online status
            userDAO.updateUserOnlineStatus(userId, true);
            
            // Add to active clients
            TCPServer.addClient(userId, this);
            
            // Send success response
            JsonObject response = new JsonObject();
            response.addProperty("type", "login_success");
            response.addProperty("userId", userId);
            response.addProperty("username", user.getUsername());
            response.addProperty("fullName", user.getFullName());
            sendMessage(gson.toJson(response));
            
        } else {
            sendErrorMessage("Invalid username or password");
        }
    }
    
    private void handleChatMessage(JsonObject jsonMessage) {
        if (userId == null) {
            sendErrorMessage("Not authenticated");
            return;
        }
        
        Long recipientId = jsonMessage.get("recipientId").getAsLong();
        String content = SecurityUtil.sanitizeInput(jsonMessage.get("content").getAsString());
        
        User recipient = userDAO.getUserById(recipientId);
        if (recipient != null) {
            // Save message to database
            Message message = new Message(user, recipient, content);
            messageDAO.saveMessage(message);
            
            // Send message to recipient if online via TCP
            JsonObject messageToSend = new JsonObject();
            messageToSend.addProperty("type", "new_message");
            messageToSend.addProperty("senderId", userId);
            messageToSend.addProperty("senderName", user.getFullName());
            messageToSend.addProperty("content", content);
            messageToSend.addProperty("timestamp", message.getSentAt().toString());
            
            TCPServer.sendMessageToUser(gson.toJson(messageToSend), recipientId);
            
            // Also broadcast to WebSocket clients
            try {
                Class<?> webSocketClass = Class.forName("com.example.chat.WebSocket.WebSocketServer");
                java.lang.reflect.Method broadcastMethod = webSocketClass.getMethod("broadcastMessage", String.class);
                broadcastMethod.invoke(null, gson.toJson(messageToSend));
            } catch (Exception e) {
                System.err.println("WebSocket broadcast failed: " + e.getMessage());
            }
            
            // Confirm message sent
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "message_sent");
            confirmation.addProperty("messageId", message.getId());
            sendMessage(gson.toJson(confirmation));
        } else {
            sendErrorMessage("Recipient not found");
        }
    }
    
    private void handleLogout() {
        if (userId != null) {
            userDAO.updateUserOnlineStatus(userId, false);
            TCPServer.removeClient(userId);
        }
        cleanup();
    }
    
    private void handleGetUsers() {
        if (userId == null) {
            sendErrorMessage("Not authenticated");
            return;
        }
        
        JsonObject response = new JsonObject();
        response.addProperty("type", "users_list");
        // Add users list logic here
        sendMessage(gson.toJson(response));
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    private void sendErrorMessage(String error) {
        JsonObject errorMsg = new JsonObject();
        errorMsg.addProperty("type", "error");
        errorMsg.addProperty("message", error);
        sendMessage(gson.toJson(errorMsg));
    }
    
    private void cleanup() {
        try {
            if (userId != null) {
                userDAO.updateUserOnlineStatus(userId, false);
                TCPServer.removeClient(userId);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
    
    public Long getUserId() {
        return userId;
    }
}
