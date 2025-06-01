package com.example.chat.TCP.Client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Gson gson;
    protected boolean isConnected = false;
    
    public TCPClient() {
        this.gson = new Gson();
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            
            // Start listening for server messages
            new Thread(this::listenForMessages).start();
            
            System.out.println("Connected to TCP server");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to TCP server: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        try {
            if (isConnected) {
                sendLogout();
                isConnected = false;
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public void sendLogin(String username, String password) {
        JsonObject loginMsg = new JsonObject();
        loginMsg.addProperty("type", "login");
        loginMsg.addProperty("username", username);
        loginMsg.addProperty("password", password);
        
        sendMessage(gson.toJson(loginMsg));
    }
    
    public void sendChatMessage(Long recipientId, String content) {
        JsonObject chatMsg = new JsonObject();
        chatMsg.addProperty("type", "chat");
        chatMsg.addProperty("recipientId", recipientId);
        chatMsg.addProperty("content", content);
        
        sendMessage(gson.toJson(chatMsg));
    }
    
    public void sendLogout() {
        JsonObject logoutMsg = new JsonObject();
        logoutMsg.addProperty("type", "logout");
        
        sendMessage(gson.toJson(logoutMsg));
    }
    
    protected void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }
    }
    
    protected void handleServerMessage(String message) {
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String type = jsonMessage.get("type").getAsString();
            
            switch (type) {
                case "login_success":
                    System.out.println("Login successful!");
                    if (jsonMessage.has("fullName")) {
                        System.out.println("Welcome: " + jsonMessage.get("fullName").getAsString());
                    }
                    break;
                case "new_message":
                    String senderName = jsonMessage.get("senderName").getAsString();
                    String content = jsonMessage.get("content").getAsString();
                    String timestamp = jsonMessage.get("timestamp").getAsString();
                    System.out.println("\n[" + timestamp + "] " + senderName + ": " + content);
                    break;
                case "message_sent":
                    System.out.println("Message sent successfully");
                    break;
                case "error":
                    System.err.println("Error: " + jsonMessage.get("message").getAsString());
                    break;
                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Error handling server message: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    // Keep the main method for standalone testing
    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        Scanner scanner = new Scanner(System.in);
        
        if (!client.connect()) {
            return;
        }
        
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        client.sendLogin(username, password);
        
        System.out.println("Type 'quit' to exit or start chatting:");
        System.out.println("Format: recipientId:message");
        
        String input;
        while (!(input = scanner.nextLine()).equals("quit")) {
            if (input.contains(":")) {
                String[] parts = input.split(":", 2);
                try {
                    Long recipientId = Long.parseLong(parts[0]);
                    String message = parts[1];
                    client.sendChatMessage(recipientId, message);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid recipient ID format");
                }
            } else {
                System.out.println("Format: recipientId:message");
            }
        }
        
        client.disconnect();
        scanner.close();
    }
}
