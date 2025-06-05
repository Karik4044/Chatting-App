package com.example.chat.controller;

import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserDAO userDAO = new UserDAO();

    @PostMapping("/register")
    public ResponseEntity<String> register(
        @RequestParam("username") String username,
        @RequestParam("password") String password
    ) {
        if (userDAO.getUserByUsername(username) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        if (userDAO.registerUser(username, hash)) {
            return ResponseEntity.ok("Registration successful");
        }
        return ResponseEntity.status(500).body("Registration failed");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password
    ) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("Invalid input: username=" + username + ", password=" + password);
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return ResponseEntity.status(401).body("Username does not exist");
        }
        boolean passwordMatch = BCrypt.checkpw(password, user.getPasswordHash());
        System.out.println("User found: " + username);
        System.out.println("Password match: " + passwordMatch);
        System.out.println("Provided password: " + password);
        if (passwordMatch) {
            System.out.println("Login successful for user: " + username);
            return ResponseEntity.ok(user);
        } else {
            System.out.println("Invalid password for user: " + username);
            return ResponseEntity.status(401).body("Invalid password");
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userDAO.getAllUsers());
    }
}