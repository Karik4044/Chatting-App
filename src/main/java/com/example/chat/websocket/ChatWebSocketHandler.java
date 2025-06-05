package com.example.chat.websocket;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Lưu các session đang kết nối
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> currentTarget = new ConcurrentHashMap<>();
    private final Map<String, Boolean> isGroupChat = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.put(username, session);
            System.out.println("User connected: " + username);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.remove(username);
            currentTarget.remove(session.getId());
            isGroupChat.remove(session.getId());
            System.out.println("User disconnected: " + username);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = session.getId();
        String username = (String) session.getAttributes().get("username");

        System.out.println("Received message from " + username + ": " + payload);

        // Xử lý lệnh chọn chat
        if (payload.startsWith("/chat ")) {
            String target = payload.substring(6).trim();
            currentTarget.put(sessionId, target);
            isGroupChat.put(sessionId, false);
            System.out.println("User " + username + " selected chat with " + target);
            return;
        }
        if (payload.startsWith("/group ")) {
            String group = payload.substring(7).trim();
            currentTarget.put(sessionId, group);
            isGroupChat.put(sessionId, true);
            System.out.println("User " + username + " selected group " + group);
            return;
        }

        // Xử lý tin nhắn JSON
        try {
            JsonNode msgNode = objectMapper.readTree(payload);
            String content = msgNode.get("content").asText();
            String senderUsername = msgNode.get("senderUsername").asText();
            String targetUsername = msgNode.get("targetUsername").asText();
            boolean isGroup = msgNode.get("isGroup").asBoolean();

            System.out.println("Processing message: " + content + " from " + senderUsername + " to " + targetUsername);

            // Lưu tin nhắn vào database
            User sender = userDAO.getUserByUsername(senderUsername);
            if (sender == null) {
                System.err.println("Sender not found: " + senderUsername);
                return;
            }

            Message msg = new Message();
            msg.setSenderId(sender.getId());
            msg.setContent(content);
            msg.setTimeStamp(LocalDateTime.now());

            if (isGroup) {
                msg.setGroupName(targetUsername);
            } else {
                User receiver = userDAO.getUserByUsername(targetUsername);
                if (receiver != null) {
                    msg.setReceiverId(receiver.getId());
                } else {
                    System.err.println("Receiver not found: " + targetUsername);
                    return;
                }
            }

            // Lưu vào database
            messageDAO.saveMessage(msg);
            System.out.println("Message saved to database successfully");

            // Gửi tin nhắn đến người nhận
            if (isGroup) {
                broadcastToGroup(targetUsername, payload, senderUsername);
            } else {
                WebSocketSession receiverSession = sessions.get(targetUsername);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.sendMessage(new TextMessage(payload));
                    System.out.println("Message sent to " + targetUsername);
                } else {
                    System.out.println("Receiver " + targetUsername + " is not online");
                }
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void broadcastToGroup(String groupName, String message, String senderUsername) {
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            String username = entry.getKey();
            WebSocketSession session = entry.getValue();
            
            if (!username.equals(senderUsername) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    System.out.println("Group message sent to " + username);
                } catch (Exception e) {
                    System.err.println("Error sending group message to " + username + ": " + e.getMessage());
                }
            }
        }
    }
}