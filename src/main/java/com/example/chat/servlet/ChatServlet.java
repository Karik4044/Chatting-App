package com.example.chat.servlet;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws/chat/{username}")
public class ChatServlet {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final UserDAO userDAO = new UserDAO();
    private static final MessageDAO messageDAO = new MessageDAO();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
    }

    @OnMessage
    public void onMessage(String messageJson, Session session, @PathParam("username") String username) throws IOException {
        // messageJson should contain: { "target": "...", "type": "private"/"group", "content": "..." }
        MessagePayload payload = gson.fromJson(messageJson, MessagePayload.class);
        if ("private".equals(payload.type)) {
            User sender = userDAO.getUserByUsername(username);
            User receiver = userDAO.getUserByUsername(payload.target);
            if (sender != null && receiver != null) {
                Message msg = new Message(sender.getId(), receiver.getId(), payload.content);
                messageDAO.saveMessage(msg);
                String msgJson = gson.toJson(msg);
                sendToUser(username, msgJson);
                sendToUser(payload.target, msgJson);
            }
        } else if ("group".equals(payload.type)) {
            User sender = userDAO.getUserByUsername(username);
            if (sender != null) {
                Message msg = new Message(sender.getId(), null, payload.content);
                msg.setGroupName(payload.target);
                messageDAO.saveMessage(msg);
                String msgJson = gson.toJson(msg);
                // Broadcast to all sessions (simple group logic)
                for (Session s : sessions.values()) {
                    s.getBasicRemote().sendText(msgJson);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Log error
    }

    private void sendToUser(String username, String message) throws IOException {
        Session s = sessions.get(username);
        if (s != null && s.isOpen()) {
            s.getBasicRemote().sendText(message);
        }
    }

    // Helper class for message payload
    private static class MessagePayload {
        String target;
        String type;
        String content;
    }
}