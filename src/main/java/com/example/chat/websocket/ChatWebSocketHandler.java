package com.example.chat.websocket;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.example.chat.TCP.Server.TCPServer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler implements TCPServer.WebSocketMessageHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final TCPServer tcpServer;

    public ChatWebSocketHandler(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
        this.tcpServer.setWebSocketHandler(this);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.put(username, session);
            tcpServer.broadcastGlobal(username + " Online!");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            session.sendMessage(new TextMessage("Error: Not authenticated"));
            return;
        }

        String payload = message.getPayload();
        String[] parts = payload.split(":", 2);
        String command = parts[0].trim();
        String content = parts.length > 1 ? parts[1].trim() : "";

        UserDAO userDAO = new UserDAO();
        MessageDAO messageDAO = new MessageDAO();
        User sender = userDAO.getUserByUsername(username);

        if (sender == null) {
            session.sendMessage(new TextMessage("Error: User not found"));
            return;
        }

        if (command.equals("/chat")) {
            User target = userDAO.getUserByUsername(content);
            if (target == null) {
                session.sendMessage(new TextMessage("User not found"));
                return;
            }
            session.getAttributes().put("target", content);
            session.getAttributes().put("isGroup", false);
            loadChatHistory(session, sender.getId(), target.getId());
        } else if (command.equals("/group")) {
            session.getAttributes().put("target", content);
            session.getAttributes().put("isGroup", true);
            loadGroupChatHistory(session, content);
        } else {
            String target = (String) session.getAttributes().get("target");
            if (target == null) {
                session.sendMessage(new TextMessage("Please select a chat target with /chat or /group"));
                return;
            }

            boolean isGroup = Boolean.TRUE.equals(session.getAttributes().get("isGroup"));
            Message msg;
            if (isGroup) {
                msg = new Message(sender.getId(), null, content);
                msg.setGroupName(target);
            } else {
                User targetUser = userDAO.getUserByUsername(target);
                if (targetUser == null) {
                    session.sendMessage(new TextMessage("Target user not found"));
                    return;
                }
                msg = new Message(sender.getId(), targetUser.getId(), content);
            }
            messageDAO.saveMessage(msg);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedMessage = String.format("[%s] [%s->%s]: %s",
                    LocalDateTime.now().format(fmt), username, target, content);

            if (isGroup) {
                for (WebSocketSession s : sessions.values()) {
                    if (Boolean.TRUE.equals(s.getAttributes().get("isGroup")) &&
                            target.equals(s.getAttributes().get("target"))) {
                        sendMessage(s, formattedMessage);
                    }
                }
            } else {
                WebSocketSession targetSession = sessions.get(target);
                if (targetSession != null && targetSession.isOpen()) {
                    sendMessage(targetSession, formattedMessage);
                }
                sendMessage(session, formattedMessage);
            }

            tcpServer.broadcastWebSocketMessage(username, target, content, isGroup);
        }
    }

    private void loadChatHistory(WebSocketSession session, Long userId1, Long userId2) throws IOException {
        List<Message> history = new MessageDAO().getMessagesBetweenUsers(userId1, userId2);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Message m : history) {
            String senderName = m.getSender().getUsername();
            String formattedMessage = String.format("[%s] [%s]: %s",
                    m.getTimeStamp().format(fmt), senderName, m.getContent());
            sendMessage(session, formattedMessage);
        }
    }

    private void loadGroupChatHistory(WebSocketSession session, String groupName) throws IOException {
        List<Message> history = new MessageDAO().getMessagesForGroup(groupName);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Message m : history) {
            String senderName = m.getSender().getUsername();
            String formattedMessage = String.format("[%s] [%s->%s]: %s",
                    m.getTimeStamp().format(fmt), senderName, groupName, m.getContent());
            sendMessage(session, formattedMessage);
        }
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.remove(username);
            tcpServer.broadcastGlobal(username + " Offline!");
        }
    }

    @Override
    public void sendMessageToWebSocket(String username, String target, String message, boolean isGroup) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedMessage = String.format("[%s] [%s->%s]: %s",
                LocalDateTime.now().format(fmt), username, target, message);

        if (isGroup) {
            for (WebSocketSession s : sessions.values()) {
                if (Boolean.TRUE.equals(s.getAttributes().get("isGroup")) &&
                        target.equals(s.getAttributes().get("target"))) {
                    try {
                        sendMessage(s, formattedMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            WebSocketSession targetSession = sessions.get(target);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    sendMessage(targetSession, formattedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            WebSocketSession senderSession = sessions.get(username);
            if (senderSession != null && senderSession.isOpen()) {
                try {
                    sendMessage(senderSession, formattedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}