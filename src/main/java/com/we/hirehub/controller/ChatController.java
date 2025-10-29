// com.we.hirehub.controller.ChatController
package com.we.hirehub.controller;

import com.we.hirehub.dto.ChatMessageRequest;
import com.we.hirehub.dto.LiveChatDto;
import com.we.hirehub.service.LiveChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final LiveChatService livechatService;

    @GetMapping("/api/chat/history/{sessionId}")
    public List<LiveChatDto> getChatHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return livechatService.getRecentMessages(sessionId, limit);
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageRequest request) {
        // 컨트롤러에서 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("컨트롤러 인증 정보: {}", auth);
        log.info("Principal: {}", auth != null ? auth.getName() : "null");

        livechatService.send(request.getSessionId(), request.getContent(), request.getNickname());
        return ResponseEntity.ok().build();
    }
}
