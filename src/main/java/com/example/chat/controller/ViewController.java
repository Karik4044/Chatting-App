package com.example.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/view-chat-alternative")
     public String chatAlternative() {
         return "chat";
     }
}