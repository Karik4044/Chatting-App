package com.example.chat.servlet;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Using @WebServlet annotation is an alternative to web.xml, but web.xml takes precedence if both exist for the same servlet name/url.
// Since web.xml defines it, this annotation is more for clarity or if web.xml entry was removed.
@WebServlet(name = "ChatServlet", urlPatterns = {"/chat"})
public class ChatServlet extends HttpServlet {

    private MessageDAO messageDAO;
    private UserDAO userDAO; // Assuming you might need user info
    private Gson gson;

    @Override
    public void init() throws ServletException {
        messageDAO = new MessageDAO();
        userDAO = new UserDAO(); // Initialize UserDAO

        // Configure Gson for LocalDateTime serialization/deserialization
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        gson = gsonBuilder.create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false); // Don't create new session if not exists

        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            // For AJAX requests, send an error; for page loads, redirect to login (not implemented here)
            if ("getMessages".equals(action) || "getUsers".equals(action)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(Collections.singletonMap("error", "User not authenticated")));
                return;
            }
            // If it's a direct GET to /chat without being logged in, redirect to a login page (conceptual)
            // response.sendRedirect("login.jsp"); // Assuming you have a login.jsp
            // For now, let's just show the chat page, which will be limited
            request.getRequestDispatcher("/chat.jsp").forward(request, response);
            return;
        }


        if ("getMessages".equals(action)) {
            String chatWithUserIdStr = request.getParameter("chatWith");
            String groupName = request.getParameter("groupName");
            List<Message> messages;

            if (chatWithUserIdStr != null && !chatWithUserIdStr.isEmpty()) {
                try {
                    Long chatWithUserId = Long.parseLong(chatWithUserIdStr);
                    messages = messageDAO.getMessagesBetweenUsers(currentUser.getId(), chatWithUserId);
                } catch (NumberFormatException e) {
                    messages = Collections.emptyList();
                }
            } else if (groupName != null && !groupName.isEmpty()) {
                messages = messageDAO.getMessagesForGroup(groupName);
            } else {
                // Default: maybe load messages for a general room or last active chat
                // For now, returning empty if no specific chat is requested
                messages = Collections.emptyList();
            }

            // Prepare messages for JSON, including sender username
            List<Map<String, Object>> messagesForJson = messages.stream().map(msg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", msg.getId());
                map.put("senderId", msg.getSenderId());
                map.put("senderUsername", msg.getSender() != null ? msg.getSender().getUsername() : "Unknown");
                map.put("receiverId", msg.getReceiverId());
                map.put("groupName", msg.getGroupName());
                map.put("content", msg.getContent());
                map.put("timeStamp", msg.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                map.put("isCurrentUser", msg.getSenderId().equals(currentUser.getId()));
                return map;
            }).collect(Collectors.toList());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(messagesForJson));

        } else if ("getUsers".equals(action)) {
            List<User> users = userDAO.getAllUsers();
            // Filter out the current user from the list
            List<User> otherUsers = users.stream()
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(otherUsers));
        }
        else {
            // Default action: forward to chat page
            // You might want to pass initial data like the current user
            request.setAttribute("currentUser", currentUser);
            request.getRequestDispatcher("/chat.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        Map<String, Object> jsonResponse = new HashMap<>();

        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "User not authenticated. Please login.");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }

        if ("sendMessage".equals(action)) {
            String content = request.getParameter("content");
            String receiverIdStr = request.getParameter("receiverId");
            String groupName = request.getParameter("groupName");

            if (content == null || content.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Message content cannot be empty.");
            } else {
                Message newMessage = new Message();
                newMessage.setSenderId(currentUser.getId());
                newMessage.setContent(content);
                newMessage.setTimeStamp(LocalDateTime.now());

                boolean messageSaved = false;
                if (receiverIdStr != null && !receiverIdStr.isEmpty()) {
                    try {
                        Long receiverId = Long.parseLong(receiverIdStr);
                        if (userDAO.getUserById(receiverId) == null) {
                            jsonResponse.put("status", "error");
                            jsonResponse.put("message", "Receiver not found.");
                        } else {
                            newMessage.setReceiverId(receiverId);
                            messageSaved = messageDAO.saveMessage(newMessage);
                        }
                    } catch (NumberFormatException e) {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", "Invalid receiver ID.");
                    }
                } else if (groupName != null && !groupName.isEmpty()) {
                    newMessage.setGroupName(groupName);
                    messageSaved = messageDAO.saveMessage(newMessage);
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Receiver ID or Group Name is required.");
                }

                if (messageSaved) {
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Message sent!");
                    // Optionally return the saved message
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("id", newMessage.getId()); // ID will be generated after save
                    msgMap.put("senderId", newMessage.getSenderId());
                    msgMap.put("senderUsername", currentUser.getUsername());
                    msgMap.put("receiverId", newMessage.getReceiverId());
                    msgMap.put("groupName", newMessage.getGroupName());
                    msgMap.put("content", newMessage.getContent());
                    msgMap.put("timeStamp", newMessage.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    msgMap.put("isCurrentUser", true);
                    jsonResponse.put("sentMessage", msgMap);

                } else if (!jsonResponse.containsKey("message")){ // if no specific error was set before
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Failed to send message.");
                }
            }
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid action.");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}