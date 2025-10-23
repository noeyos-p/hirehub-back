package com.we.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {

    @GetMapping("/join/chat")
    public String chat(){ return "join/chat"; }

    @GetMapping("/join/agent")
    public String agent(){ return "join/agent"; }
}
