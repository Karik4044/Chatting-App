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
import java.io.IOException;


public class TCPServer implements Runnable {
    private final List<ConnectionHandler> connections = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private boolean done = false;
    private ExecutorService pool;
    public TCPServer() {}

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8082);
            pool = Executors.newCachedThreadPool();
            System.out.println("TCP Server started on port 8082");
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

    public void broadcastGlobal(String message) {
        for (ConnectionHandler ch : connections) {
            if (!ch.isInChat()) {
                ch.sendMessage(message);
            }
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
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
        for (ConnectionHandler ch : connections) {
            ch.shutdown();
        }
    }

    public List<ConnectionHandler> getConnections() {
        return connections;
    }

    public static class ConnectionHandler implements Runnable {
        private final Socket clientSocket;
        private final TCPServer server;
        private BufferedReader in;
        private PrintWriter out;
        private boolean authenticated = false;
        private User currentUser;
        private String username;
        private final UserDAO userDAO = new UserDAO();
        private volatile String currentChatTarget = null;
        private volatile boolean isGroupChat = false;

        public ConnectionHandler(Socket socket, TCPServer server) {
            this.clientSocket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    // NEW: Add a special command for the gateway to forward messages without login
                    if (message.startsWith("/forward ")) {
                        handleForwardCommand(message);
                        // Since this is a one-off command from the gateway, we can close the connection.
                        shutdown();
                        break;
                    } else {
                        handleMessage(message); // Original message handling
                    }
                }
            } catch (IOException e) {
                // No need to call shutdown() here as it's already in the finally block of the caller or handled inside the loop.
            } finally {
                shutdown();
            }
        }

        // NEW: Handler for the /forward command
        private void handleForwardCommand(String command) {
            // Format: /forward <isGroup> <sender> <target> <content>
            String[] parts = command.split(" ", 5);
            if (parts.length < 5) {
                System.err.println("Invalid /forward command: " + command);
                return;
            }

            boolean isGroup = Boolean.parseBoolean(parts[1]);
            String senderUsername = parts[2];
            String targetName = parts[3];
            String content = parts[4];

            System.out.println("TCP Server: Forwarding message from " + senderUsername + " to " + targetName);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String ts = LocalDateTime.now().format(fmt);

            if (isGroup) {
                String formattedGroupMessage = String.format("[%s] [%s->%s]: %s",
                        ts,
                        senderUsername,
                        targetName,
                        content);

                // Broadcast to all connected clients currently in that group chat
                for (ConnectionHandler ch : server.getConnections()) {
                    if (ch.isGroupChat && targetName.equals(ch.currentChatTarget)) {
                        ch.sendMessage(formattedGroupMessage);
                    }
                }
            } else {
                // Send to a specific user if they are connected
                String formattedMessage = String.format("[%s] [%s]: %s", ts, senderUsername, content);
                server.sendPrivate(targetName, formattedMessage);
            }
        }

        public void loadChatHistory() {
            out.println("=== Chat history with " + currentChatTarget + " ===");
            List<Message> history;
            User target = null;
            if (isGroupChat) {
                history = new MessageDAO().getMessagesForGroup(currentChatTarget);
            } else {
                target = userDAO.getUserByUsername(currentChatTarget);
                if (target == null) {
                    out.println("User not found.");
                    return;
                }
                history = new MessageDAO().getMessagesBetweenUsers(currentUser.getId(), target.getId());
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Message m : history) {
                // FIX: Use the sender object (m.getSender()) which was fetched by the DAO.
                // This is the correct way to access sender's information.
                String senderUsername = m.getSender() != null ? m.getSender().getUsername() : "unknown";
                String senderDisplayName = m.getSender().getId().equals(currentUser.getId()) ? "You" : senderUsername;

                String arrow = "";
                if (!isGroupChat) {
                    arrow = "->" + currentChatTarget;
                } else {
                    arrow = "->" + currentChatTarget; // For group
                }

                out.println(String.format("[%s] [%s%s]: %s",
                        m.getTimeStamp().format(fmt), // FIX: Use getTimeStamp() for consistency
                        senderDisplayName,
                        arrow,
                        m.getContent()));
            }
        }

        public boolean isInChat() {
            return currentChatTarget != null;
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
                        out.println("ERROR: Username already exists.");
                    } else {
                        String hash = BCrypt.hashpw(regPass, BCrypt.gensalt());
                        if (userDAO.registerUser(regUser, hash)) {
                            out.println("SUCCESS: Registration successful.");
                        } else {
                            out.println("ERROR: Registration failed.");
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
                        username = user.getUsername();
                        out.println("SUCCESS: Login successful! Welcome, " + username);
                        server.broadcastGlobal(username + " Online!");
                    } else {
                        out.println("ERROR: Login failed. Invalid username or password.");
                    }
                }
            } catch (Exception e) {
                out.println("ERROR: " + e.getMessage());
            }
        }

        private void handleMessage(String message) {
            if (message == null || message.trim().isEmpty()) {
                return;
            }

            message = message.trim();
            System.out.println("Received: " + message + " from " + (username != null ? username : "unauthenticated user"));

            // Cho phép /users command mà không cần authenticate
            if (message.equals("/users")) {
                handleGetUsers();
                return;
            }

            if (!authenticated) {
                handleAuth(message);
                return;
            }

            // Gọi handleChat cho các message khác khi đã authenticated
            handleChat(message);
        }

        private void handleChat(String message) {
            if (message.startsWith("/chat ")) {
                String targetName = message.substring(6).trim();
                if (targetName.equals(currentUser.getUsername())) {
                    out.println("Cannot chat with yourself.");
                    return;
                }
                if (userDAO.getUserByUsername(targetName) != null) {
                    currentChatTarget = targetName;
                    isGroupChat = false;
                    loadChatHistory();
                } else {
                    out.println("User not found.");
                }
                return;
            }
            
            if (message.startsWith("/group ")) {
                String groupName = message.substring(7).trim();
                currentChatTarget = groupName;
                isGroupChat = true;
                loadChatHistory();
                out.println("Entered group chat: " + groupName);
                return;
            }

            if (currentChatTarget == null) {
                out.println("Please select a chat target with /chat <username> or /group <groupname>.");
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String ts = LocalDateTime.now().format(fmt);

            if (isGroupChat) {
                // FIX: Create and save group messages correctly
                Message groupMsg = new Message();
                groupMsg.setSender(currentUser); // Set the full User object
                groupMsg.setGroupName(currentChatTarget);
                groupMsg.setContent(message);
                groupMsg.setTimeStamp(LocalDateTime.now());
                new MessageDAO().saveMessage(groupMsg);

                String formattedGroupMessage = String.format("[%s] [%s->%s]: %s",
                        ts,
                        currentUser.getUsername(),
                        currentChatTarget,
                        message);

                for (ConnectionHandler ch : server.getConnections()) {
                    if (ch.isGroupChat && currentChatTarget.equals(ch.currentChatTarget)) {
                        ch.sendMessage(formattedGroupMessage);
                    }
                }

            } else {
                User targetUser = userDAO.getUserByUsername(currentChatTarget);
                if (targetUser == null) {
                    out.println("Error: Target user " + currentChatTarget + " not found for sending message.");
                    currentChatTarget = null;
                    return;
                }
                // FIX: Create Message object correctly by setting the sender User object.
                // This is the main reason messages were not saving from the TCP client.
                Message privateMsg = new Message();
                privateMsg.setSender(currentUser);
                privateMsg.setReceiverId(targetUser.getId());
                privateMsg.setContent(message);
                privateMsg.setTimeStamp(LocalDateTime.now());
                new MessageDAO().saveMessage(privateMsg);

                String formattedMessage = String.format("[%s] [%s]: %s", ts, currentUser.getUsername(), message);
                server.sendPrivate(targetUser.getUsername(), formattedMessage);
                out.println(String.format("[%s] [You->%s]: %s", ts, targetUser.getUsername(), message));
            }
        }

        private void handleGetUsers() {
            try {
                List<User> users = userDAO.getAllUsers();
                StringBuilder response = new StringBuilder("[");
                
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    response.append("{\"id\":").append(user.getId())
                           .append(",\"username\":\"").append(user.getUsername()).append("\"}");
                    if (i < users.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("]");
                
                out.println(response.toString());
                System.out.println("Sent users list to client: " + response.toString());
                
            } catch (Exception e) {
                e.printStackTrace();
                out.println("ERROR: Failed to get users - " + e.getMessage());
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public String getUsername() {
            return username;
        }

        public void shutdown() {
            try {
                server.connections.remove(this);
                if (username != null && authenticated) {
                    server.broadcastGlobal(username + " Offline.");
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed())
                    clientSocket.close();
            } catch (IOException ignored) {}
            System.out.println((username != null ? username : "A client") + " disconnected.");
        }
    }
}