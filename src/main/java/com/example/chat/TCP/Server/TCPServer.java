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

    /**
     * Chỉ gửi thông báo chung tới những ai chưa vào private/group chat
     */
    public void broadcastGlobal(String message) {
        for (ConnectionHandler ch : connections) {
            if (!ch.isInChat()) {
                ch.sendMessage(message);
            }
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
        // MODIFIED: Added volatile
        private volatile String currentChatTarget = null; // username or group name
        private volatile boolean isGroupChat = false;

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

        private void loadChatHistory() {
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
                boolean sentByMe = !isGroupChat && target != null && m.getSenderId().equals(currentUser.getId()) && m.getReceiverId().equals(target.getId());
                boolean receivedByMe = !isGroupChat && target != null && m.getSenderId().equals(target.getId()) && m.getReceiverId().equals(currentUser.getId());
                if (isGroupChat || sentByMe || receivedByMe) {
                    String sender = m.getSenderId().equals(currentUser.getId()) ? "You" : m.getSenderUsername();
                    String arrow = "";
                    if (!isGroupChat && m.getSenderId().equals(currentUser.getId())) {
                         arrow = "->" + currentChatTarget;
                    } else if (isGroupChat) {
                         arrow = "->" + currentChatTarget; // For group, sender is shown, then arrow to group
                    }

                    out.println(String.format("[%s] [%s%s]: %s",
                            m.getCreatedAt().format(fmt),
                            sender,
                            arrow,
                            m.getContent()));
                }
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
                        username = user.getUsername();
                        out.println("Login successful! Welcome, " + username);
                        server.broadcastGlobal(username + " Online!");
                        out.println("Type /chat <username> to start a private chat or /group <groupname> for group chat.");
                    } else {
                        out.println("Login failed. Invalid username or password.");
                    }

                } else {
                    out.println("Please /register or /login to continue.");
                }
            } catch (Exception e) {
                out.println("Error during authentication: " + e.getMessage());
                e.printStackTrace(); // For server-side debugging
            }
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
                // Implement group existence check if needed
                currentChatTarget = groupName;
                isGroupChat = true;
                loadChatHistory();
                // You might want to announce to the user they've joined/switched to a group chat
                out.println("Entered group chat: " + groupName);
                return;
            }

            if (currentChatTarget == null) {
                out.println("Please select a chat target with /chat <username> or /group <groupname>.");
                // Or, treat as a global message if your application supports it.
                // For now, it requires a target.
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String ts = LocalDateTime.now().format(fmt);

            if (isGroupChat) {
                Message groupMsg = new Message(currentUser.getId(), null, message); // ReceiverId is null for group messages
                groupMsg.setGroupName(currentChatTarget);
                // groupMsg.setCreatedAt(LocalDateTime.now()); // Constructor sets this
                new MessageDAO().saveMessage(groupMsg);

                String formattedGroupMessage = String.format("[%s] [%s->%s]: %s",
                        ts,
                        currentUser.getUsername(),
                        currentChatTarget, // Group name
                        message);

                for (ConnectionHandler ch : server.getConnections()) {
                    // Send only to users who are currently "in" this specific group chat
                    // This simple check means they have to have typed /group <groupName>
                    // More advanced group logic would involve group membership lists.
                    if (ch.isGroupChat && currentChatTarget.equals(ch.currentChatTarget)) {
                        ch.sendMessage(formattedGroupMessage);
                    }
                }
            } else { // Private chat
                User targetUser = userDAO.getUserByUsername(currentChatTarget);
                if (targetUser == null) {
                    out.println("Error: Target user " + currentChatTarget + " not found for sending message.");
                    currentChatTarget = null; // Reset chat target
                    return;
                }
                Message privateMsg = new Message(currentUser.getId(), targetUser.getId(), message);
                // privateMsg.setCreatedAt(LocalDateTime.now()); // Constructor sets this
                new MessageDAO().saveMessage(privateMsg);

                // Send to target recipient
                server.sendPrivate(targetUser.getUsername(),
                        String.format("[%s] [%s]: %s", ts, currentUser.getUsername(), message));
                // Echo to sender
                out.println(String.format("[%s] [You->%s]: %s", ts, targetUser.getUsername(), message));
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
                server.connections.remove(this); // Remove from active connections
                if (username != null && authenticated) {
                    server.broadcastGlobal(username + " Offline.");
                }
                if (in  != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed())
                    clientSocket.close();
            } catch (IOException ignored) {}
            System.out.println( (username != null ? username : "A client") + " disconnected.");
        }
    }
}