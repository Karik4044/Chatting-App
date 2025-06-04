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
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
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
    public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
        User user = userDAO.getUserByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(null);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userDAO.getAllUsers());
    }
}