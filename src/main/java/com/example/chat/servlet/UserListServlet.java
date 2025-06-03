// src/main/java/com/example/chat/servlet/UserListServlet.java
package com.example.chat.servlet;

import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.User;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/users")
public class UserListServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"User not authenticated.\"}"); // Send JSON error
            resp.setContentType("application/json;charset=UTF-8");
            return;
        }

        List<User> allUsers = userDAO.getAllUsers(); // Đảm bảo UserDAO.getAllUsers() đã được triển khai
        List<User> otherUsers = new ArrayList<>();

        if (allUsers != null) {
            Long currentUserId = currentUser.getId();
            otherUsers = allUsers.stream()
                    .filter(user -> user.getId() != null && !user.getId().equals(currentUserId))
                    .collect(Collectors.toList());
        }

        resp.setContentType("application/json;charset=UTF-8");
        gson.toJson(otherUsers, resp.getWriter());
    }
}