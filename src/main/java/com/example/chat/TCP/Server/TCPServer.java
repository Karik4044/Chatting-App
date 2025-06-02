package com.example.chat.TCP.Server;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Runnable {
    private final List<ConnectionHandler> connections = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private boolean done = false;
    private ExecutorService pool;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8080);
            pool = Executors.newCachedThreadPool();
            System.out.println("TCP Server started on port 8080");
            while (!done) {
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(clientSocket, this);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            ch.sendMessage(message);
        }
    }

    public void sendPrivate(String targetName, String message) {
        for (ConnectionHandler ch : connections) {
            if (targetName.equals(ch.getUsername())) {
                ch.sendMessage(message);
                break;
            }
        }
    }

    public void shutdown() {
        done = true;
        try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); }
        catch (IOException ignored) {}
        for (ConnectionHandler ch : connections) {
            ch.shutdown();
        }
    }

    public static void main(String[] args) {
        new TCPServer().run();
    }

    public List<ConnectionHandler> getConnections() { return connections; }

    public static class ConnectionHandler implements Runnable {
        private final Socket clientSocket;
        private final TCPServer server;
        private BufferedReader in;
        private PrintWriter out;
        private boolean authenticated = false;
        private User currentUser;
        private String username;
        private final UserDAO userDAO = new UserDAO();

        public ConnectionHandler(Socket socket, TCPServer server) {
            this.clientSocket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("Welcome to the chat server!");
                out.println("Please /register <username> <password> or /login <username> <password> to continue.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (!authenticated) {
                        handleAuth(message);
                    } else {
                        handleChat(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        private void handleAuth(String message) {
            try {
                if (message.startsWith("/register ")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3) {
                        out.println("Usage: /register <username> <password>");
                        return;
                    }
                    String regUser = parts[1], regPass = parts[2];
                    if (userDAO.getUserByUsername(regUser) != null) {
                        out.println("Username already exists.");
                    } else {
                        String hash = BCrypt.hashpw(regPass, BCrypt.gensalt());
                        if (userDAO.registerUser(regUser, hash)) {
                            out.println("Registration successful. Please /login <username> <password>.");
                        } else {
                            out.println("Registration failed. Try again.");
                        }
                    }

                } else if (message.startsWith("/login ")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3) {
                        out.println("Usage: /login <username> <password>");
                        return;
                    }
                    String loginUser = parts[1], loginPass = parts[2];
                    User user = userDAO.getUserByUsername(loginUser);

                    if (user != null && BCrypt.checkpw(loginPass, user.getPasswordHash())) {
                        authenticated = true;
                        currentUser = user;
                        username    = user.getUsername();
                        out.println("Login successful! Welcome, " + username);

                        // === LOAD LỊCH SỬ Ở ĐÂY ===
                        List<Message> history = new MessageDAO().getMessagesForUser(currentUser.getId());
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        for (Message m : history) {
                            out.println(String.format("[%s] [%s]: %s",
                                m.getCreatedAt().format(fmt),
                                m.getSenderUsername(),
                                m.getContent()));
                        }
                        // ==========================

                        server.broadcast(username + " has joined the chat!");
                    } else {
                        out.println("Login failed. Invalid username or password.");
                    }

                } else {
                    out.println("Please /register or /login to continue.");
                }
            } catch (Exception e) {
                out.println("Error during authentication: " + e.getMessage());
            }
        }

        public User getCurrentUser() { return currentUser; }

        private void handleChat(String message) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String ts = LocalDateTime.now().format(fmt);

            if (!message.startsWith("@")) {
                // broadcast
                String text = message;
                // 1) với mỗi kết nối đã auth, lưu message → FK ok
                for (ConnectionHandler ch : server.getConnections()) {
                    if (ch.authenticated) {
                        Message msg = new Message(
                          currentUser.getId(),
                          ch.getCurrentUser().getId(),
                          text
                        );
                        new MessageDAO().saveMessage(msg);
                        // 2) gửi thật
                        ch.sendMessage(
                          String.format("[%s] [%s]: %s", ts, currentUser.getUsername(), text)
                        );
                    }
                }
            }
            // … các case @private, /nick, /quit giữ nguyên
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public String getUsername() {
            return username;
        }

        public void shutdown() {
            try {
                if (in  != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed())
                    clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}

