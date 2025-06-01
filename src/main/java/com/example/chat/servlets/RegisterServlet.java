package com.example.chat.servlets;

import com.example.chat.DAO.UserDAO;
import com.example.chat.model.User;
import com.example.chat.util.HibernateUtil;
import com.example.chat.util.SecurityUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDAO userDAO;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Username validation pattern (alphanumeric and underscore only)
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Forward to register.jsp
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Get parameters and sanitize
        String username = SecurityUtil.sanitizeInput(req.getParameter("username"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String email = SecurityUtil.sanitizeInput(req.getParameter("email"));

        // Basic validation
        String validationError = validateInput(username, password, confirmPassword, email);
        if (validationError != null) {
            req.setAttribute("error", validationError);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        Transaction transaction = null;
        Session session = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Check if username already exists
            if (userDAO.userExists(username)) {
                req.setAttribute("error", "Username already exists. Please choose a different username.");
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }
            
            // Check if email already exists
            if (userDAO.emailExists(email)) {
                req.setAttribute("error", "Email already registered. Please use a different email.");
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(SecurityUtil.hashPassword(password)); // Hash password
            newUser.setEmail(email);
            newUser.setOnline(false);
            newUser.setCreatedAt(LocalDateTime.now());

            // Save user
            userDAO.saveUser(newUser);
            transaction.commit();
            
            // Registration successful
            req.setAttribute("success", "Registration successful! Please login to continue.");
            resp.sendRedirect(req.getContextPath() + "/login?registered=true");
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            req.setAttribute("error", "An error occurred during registration. Please try again.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Validate user input
     */
    private String validateInput(String username, String password, String confirmPassword, String email) {
        // Check for null or empty fields
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Please confirm your password.";
        }
        
        if (email == null || email.trim().isEmpty()) {
            return "Email is required.";
        }
        
        // Validate username format
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "Username must be 3-20 characters long and contain only letters, numbers, and underscores.";
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        
        // Validate password length
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }
        
        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        
        // Password strength validation
        if (!isPasswordStrong(password)) {
            return "Password must contain at least one uppercase letter, one lowercase letter, and one number.";
        }
        
        return null; // No validation errors
    }
    
    /**
     * Check password strength
     */
    private boolean isPasswordStrong(String password) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
}
