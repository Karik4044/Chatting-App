package com.example.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // chuyển tới /chat
        return "redirect:/chat";
    }

    // Phương thức mới để xử lý /chat
    @GetMapping("/chat")
    public String showChatPage() {
        // Trả về view name "chat" (Spring MVC sẽ ghép prefix/suffix -> /jsp/chat.jsp)
        return "chat";
    }
}