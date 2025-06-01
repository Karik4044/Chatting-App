package com.example.chat.servlets;

import com.example.chat.DAO.messagedao;
import com.example.chat.DAO.UserDAO;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.util.SecurityUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private UserDAO userDAO;
    private messagedao messageDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        messageDAO = new messagedao();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }
        
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            switch (action != null ? action : "") {
                case "getUsers":
                    handleGetUsers(out);
                    break;
                case "getMessages":
                    handleGetMessages(request, out, session);
                    break;
                case "sendMessage":
                    handleSendMessage(request, out, session);
                    break;
                default:
                    sendErrorResponse(out, "Invalid action");
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private void handleGetUsers(PrintWriter out) {
        try {
            List<User> users = userDAO.getAllUsers();
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("users", gson.toJsonTree(users));
            out.write(gson.toJson(response));
        } catch (Exception e) {
            sendErrorResponse(out, "Error fetching users");
        }
    }


    
    private void handleGetMessages(HttpServletRequest request, PrintWriter out, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            String otherUserIdStr = request.getParameter("otherUserId");
            
            if (otherUserIdStr == null) {
                sendErrorResponse(out, "Other user ID is required");
                return;
            }
            
            Long otherUserId = Long.parseLong(otherUserIdStr);
            User currentUser = userDAO.getUserById(userId);
            User otherUser = userDAO.getUserById(otherUserId);
            // Assuming the int parameters are for pagination with default values
            int offset = 0;
            int limit = 50;  // Or another appropriate default
            List<Message> messages = messageDAO.getMessagesBetweenUsers(currentUser, otherUser, offset, limit);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("messages", gson.toJsonTree(messages));
            out.write(gson.toJson(response));
        } catch (Exception e) {
            sendErrorResponse(out, "Error fetching messages");
        }
    }
    
    private void handleSendMessage(HttpServletRequest request, PrintWriter out, HttpSession session) {
        try {
            Long senderId = (Long) session.getAttribute("userId");
            String receiverIdStr = request.getParameter("receiverId");
            String content = SecurityUtil.sanitizeInput(request.getParameter("content"));
            
            if (receiverIdStr == null || content == null || content.trim().isEmpty()) {
                sendErrorResponse(out, "Receiver ID and content are required");
                return;
            }
            
            Long receiverId = Long.parseLong(receiverIdStr);
            User sender = userDAO.getUserById(senderId);
            User receiver = userDAO.getUserById(receiverId);
            
            if (sender == null || receiver == null) {
                sendErrorResponse(out, "Invalid sender or receiver");
                return;
            }
            
            Message message = new Message(sender, receiver, content);
            messageDAO.saveMessage(message);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("messageId", message.getId());
            response.addProperty("timestamp", message.getSentAt().toString());
            out.write(gson.toJson(response));
            
        } catch (Exception e) {
            sendErrorResponse(out, "Error sending message");
        }
    }
    
    private void sendErrorResponse(PrintWriter out, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("error", message);
        out.write(gson.toJson(error));
    }
}
