package com.example.chat.controller;

import com.example.chat.DAO.MessageDAO;
import com.example.chat.DAO.UserDAO;
import com.example.chat.Entity.Message;
import com.example.chat.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/messages")
public class ChatController {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/chat-history")
    public ResponseEntity<List<Message>> getChatHistory(
            @RequestParam String currentUser,
            @RequestParam String target,
            @RequestParam boolean isGroup) {
        List<Message> history;
        if (isGroup) {
            history = messageDAO.getMessagesForGroup(target);
        } else {
            User targetUser = userDAO.getUserByUsername(target);
            if (targetUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            history = messageDAO.getMessagesBetweenUsers(
                    userDAO.getUserByUsername(currentUser).getId(),
                    targetUser.getId());
        }
        return ResponseEntity.ok(history);
    }
}