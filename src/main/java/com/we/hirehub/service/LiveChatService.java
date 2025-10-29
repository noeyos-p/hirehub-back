package com.we.hirehub.service;

import com.we.hirehub.dto.LiveChatDto;
import com.we.hirehub.entity.LiveChat;
import com.we.hirehub.entity.Session;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repository.LiveChatRepository;
import com.we.hirehub.repository.SessionRepository;
import com.we.hirehub.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatService {

    private final LiveChatRepository liveChatRepository;
    private final SessionRepository sessionRepository;
    private final UsersRepository usersRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<LiveChatDto> getRecentMessages(String sessionId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt"));
        List<LiveChat> chats = liveChatRepository.findBySessionId(sessionId, pageRequest);

        return chats.stream()
                .sorted((a, b) -> a.getCreateAt().compareTo(b.getCreateAt()))
                .map(chat -> toLiveChatDto(chat))
                .collect(Collectors.toList());
    }

    @Transactional
    public void send(String sessionId, String content, String requestNickname) {
        // 1. Session 가져오기 또는 생성
        Session session = sessionRepository.findById(sessionId)
                .orElseGet(() -> {
                    Session newSession = new Session();
                    newSession.setId(sessionId);
                    newSession.setCtx(new HashMap<>());
                    return sessionRepository.save(newSession);
                });

        // 2. 현재 로그인한 사용자 가져오기
        Users user = null;
        String finalNickname = "익명";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            String email = authentication.getName();
            user = usersRepository.findByEmail(email).orElse(null);

            if (user != null) {
                finalNickname = user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname()
                        : (user.getName() != null && !user.getName().isEmpty() ? user.getName() : "익명");
            }
        }

        // 3. 요청에서 nickname 있으면 우선 사용
        if (requestNickname != null && !requestNickname.trim().isEmpty()) {
            finalNickname = requestNickname.trim();
        }

        // 4. LiveChat 저장 (user_id 포함!)
        LiveChat chat = LiveChat.builder()
                .session(session)
                .content(content)
                .createAt(LocalDateTime.now())
                .user(user)  // ← 여기가 핵심! user_id 저장
                .build();

        LiveChat saved = liveChatRepository.save(chat);

        // 5. DTO 생성 (nickname은 user에서 가져오거나 request에서)
        LiveChatDto dto = LiveChatDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .createAt(saved.getCreateAt())
                .sessionId(saved.getSession().getId())
                .nickname(finalNickname)
                .build();

        // 6. WebSocket 브로드캐스트
        messagingTemplate.convertAndSend("/topic/rooms/" + sessionId, dto);
    }

    private String getNickname(String requestNickname) {
        // 1. 요청에서 닉네임이 있으면 사용
        if (requestNickname != null && !requestNickname.isEmpty()) {
            return requestNickname;
        }

        // 2. 현재 로그인한 사용자 정보 가져오기
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("인증 정보: {}", authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                String principal = authentication.getName();
                log.info("인증된 사용자 Principal: {}", principal);

                if (principal != null && !principal.equals("anonymousUser")) {
                    // 이메일로 사용자 조회
                    Users user = usersRepository.findByEmail(principal).orElse(null);

                    if (user != null) {
                        log.info("사용자 찾음 - ID: {}, 닉네임: {}, 이름: {}",
                                user.getId(), user.getNickname(), user.getName());

                        // 닉네임 우선, 없으면 이름 사용
                        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                            return user.getNickname();
                        }
                        if (user.getName() != null && !user.getName().isEmpty()) {
                            return user.getName();
                        }
                    } else {
                        log.warn("사용자를 찾을 수 없음: {}", principal);
                    }
                }
            } else {
                log.warn("인증 정보가 없거나 인증되지 않음");
            }
        } catch (Exception e) {
            log.error("닉네임 가져오기 실패", e);
        }

        // 3. 모두 실패하면 익명
        return "익명";
    }

    private LiveChatDto toLiveChatDto(LiveChat chat) {
        String nickname = "익명";

        if (chat.getUser() != null) {
            nickname = chat.getUser().getNickname() != null && !chat.getUser().getNickname().isEmpty()
                    ? chat.getUser().getNickname()
                    : (chat.getUser().getName() != null ? chat.getUser().getName() : "익명");
        } else if (chat.getSession() != null && chat.getSession().getCtx() != null) {
            // fallback: ctx에서 가져오기
            nickname = (String) chat.getSession().getCtx().getOrDefault("lastUserNickname", "익명");
        }

        return LiveChatDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .createAt(chat.getCreateAt())
                .sessionId(chat.getSession().getId())
                .nickname(nickname)
                .build();
    }
}
