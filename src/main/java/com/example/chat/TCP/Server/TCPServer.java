package com.example.chat.TCP.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning = false;

    // Store active client connections
    private static final ConcurrentHashMap<Long, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public TCPServer() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("TCP Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Handle each client in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void addClient(Long userId, ClientHandler handler) {
        activeClients.put(userId, handler);
        System.out.println("User " + userId + " connected. Active clients: " + activeClients.size());
    }

    public static void removeClient(Long userId) {
        activeClients.remove(userId);
        System.out.println("User " + userId + " disconnected. Active clients: " + activeClients.size());
    }

    public static ClientHandler getClientHandler(Long userId) {
        return activeClients.get(userId);
    }

    public static void broadcastMessage(String message, Long senderId) {
        for (ClientHandler handler : activeClients.values()) {
            if (!handler.getUserId().equals(senderId)) {
                handler.sendMessage(message);
            }
        }
    }

    public static void sendMessageToUser(String message, Long recipientId) {
        ClientHandler handler = activeClients.get(recipientId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}
