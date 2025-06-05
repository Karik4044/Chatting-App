package com.example.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // chuyển tới /FinalChatting/chat
        return "redirect:/chat";
    }

    // Phương thức mới để xử lý /chat
    @GetMapping("/chat")
    public String showChatPage() {
        return "chat.jsp"; // Spring MVC sẽ tìm /jsp/chat.jsp
    }
}