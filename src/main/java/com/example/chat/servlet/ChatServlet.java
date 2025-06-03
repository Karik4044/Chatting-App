// src/main/java/com/example/chat/servlet/ChatServlet.java
package com.example.chat.servlet;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // For LocalDateTime adapter
// import com.example.chat.util.LocalDateTimeAdapter; // You would create this adapter

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Collections;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    // Configure Gson to handle LocalDateTime if not already handled by your GSON version/setup
    // If your Message entity's timeStamp is serialized correctly by default, you might not need this.
    // private final Gson gson = new GsonBuilder()
    // .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()) // Create this adapter if needed
    // .create();
    private final Gson gson = new Gson(); // Assuming default Gson handles it or Message has transient formatted date fields


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User current = (session != null) ? (User) session.getAttribute("user") : null;

        if (current == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            gson.toJson(Collections.singletonMap("error", "User not authenticated."), resp.getWriter());
            return;
        }

        String target = req.getParameter("target");
        String type = req.getParameter("type");

        if (target == null || target.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            gson.toJson(Collections.singletonMap("error", "Target and type parameters are required."), resp.getWriter());
            return;
        }

        List<Message> history;
        try {
            if ("group".equals(type)) {
                history = messageDAO.getMessagesForGroup(target);
            } else if ("private".equals(type)) {
                User u = userDAO.getUserByUsername(target);
                if (u == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    gson.toJson(Collections.singletonMap("error", "Target user not found."), resp.getWriter());
                    return;
                }
                if (current.getId().equals(u.getId())) {
                    // Optional: Prevent fetching chat history with oneself if desired,
                    // or allow it if it's a "notes to self" feature.
                    // For now, we allow it, client-side might prevent initiating such chat.
                    history = messageDAO.getMessagesBetweenUsers(current.getId(), u.getId());
                } else {
                    history = messageDAO.getMessagesBetweenUsers(current.getId(), u.getId());
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                gson.toJson(Collections.singletonMap("error", "Invalid chat type specified."), resp.getWriter());
                return;
            }
            gson.toJson(history, resp.getWriter());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception server-side
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            gson.toJson(Collections.singletonMap("error", "An error occurred while fetching messages."), resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User current = (session != null) ? (User) session.getAttribute("user") : null;

        if (current == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            gson.toJson(Collections.singletonMap("error", "User not authenticated."), resp.getWriter());
            return;
        }

        String target = req.getParameter("target");
        String type = req.getParameter("type");
        String content = req.getParameter("message");

        if (target == null || target.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                content == null || content.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            gson.toJson(Collections.singletonMap("error", "Target, type, and message parameters are required."), resp.getWriter());
            return;
        }

        // Prevent sending messages to oneself if it's a private chat and target is self
        if ("private".equals(type) && target.equals(current.getUsername())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            gson.toJson(Collections.singletonMap("error", "Cannot send a private message to yourself."), resp.getWriter());
            return;
        }


        Message m;
        try {
            if ("group".equals(type)) {
                m = new Message(current.getId(), null, content);
                m.setGroupName(target);
                // m.setSenderUsername(current.getUsername()); // If you add senderUsername to Message entity for display
                messageDAO.saveMessage(m);
            } else if ("private".equals(type)) {
                User u = userDAO.getUserByUsername(target);
                if (u == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    gson.toJson(Collections.singletonMap("error", "Target user not found."), resp.getWriter());
                    return;
                }
                m = new Message(current.getId(), u.getId(), content);
                // m.setSenderUsername(current.getUsername()); // If you add senderUsername
                messageDAO.saveMessage(m);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                gson.toJson(Collections.singletonMap("error", "Invalid chat type specified."), resp.getWriter());
                return;
            }
            // Consider fetching the sender's username to include in the response,
            // so the client doesn't have to guess based on senderId for received messages.
            // If Message entity has a @ManyToOne User sender field that's eagerly fetched or
            // if you populate a transient senderUsername field before serializing.
            // For now, the existing Message structure is returned.
            gson.toJson(m, resp.getWriter());
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception server-side
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            gson.toJson(Collections.singletonMap("error", "An error occurred while sending the message."), resp.getWriter());
        }
    }
}