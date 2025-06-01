package com.example.chat.servlets;

import com.example.chat.DAO.UserDAO;
import com.example.chat.model.User;
import com.example.chat.util.SecurityUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // redirect đến index.jsp nếu đã login
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        // forward đến login.jsp nằm ở webapp root
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String username = SecurityUtil.sanitizeInput(req.getParameter("username"));
        String password = req.getParameter("password");
        
        if (username == null || password == null || 
            username.trim().isEmpty() || password.trim().isEmpty()) {
            req.setAttribute("error", "Username and password are required");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        
        try {
            User user = userDAO.getUserByUsername(username);
            
            if (user != null && SecurityUtil.verifyPassword(password, user.getPassword())) {
                // Update user online status
                userDAO.updateUserOnlineStatus(user.getId(), true);
                
                // Create session
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // login thành công
                resp.sendRedirect(req.getContextPath() + "/index.jsp");
            } else {
                req.setAttribute("error", "Invalid username or password");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred during login");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
