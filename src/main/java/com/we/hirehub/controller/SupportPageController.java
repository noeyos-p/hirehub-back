package com.we.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupportPageController {

    @GetMapping("/support/chat")
    public String supportChat() {
        return "support/chat";
    }

    @GetMapping("/support/agent")
    public String supportAgent() {
        return "support/agent";
    }
}
