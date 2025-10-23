// com.we.hirehub.controller.ChatController
package com.we.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    @GetMapping("/chat")
    public String chatPage() {
        return "chat"; // src/main/resources/templates/chatbot.html
    }
}
