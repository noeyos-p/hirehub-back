package com.we.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminHome(){
        return "admin/index"; // templates/admin/index.html
    }

    @GetMapping("/admin/support")
    public String adminSupport(){
        return "support/agent"; // templates/support/agent.html (대기열/수락)
    }
}
